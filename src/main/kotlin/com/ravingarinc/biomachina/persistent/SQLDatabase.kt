package com.ravingarinc.biomachina.persistent

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.ravingarinc.api.I
import com.ravingarinc.api.module.Module
import com.ravingarinc.api.module.RavinPlugin
import com.ravingarinc.api.module.SuspendingModule
import com.ravingarinc.api.module.warn
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.sqlite.SQLiteException
import java.io.File
import java.sql.Connection
import java.util.*
import java.util.logging.Level
import kotlin.coroutines.CoroutineContext

abstract class SQLDatabase(
    identifier: Class<out SQLDatabase>,
    databaseName: String,
    plugin: RavinPlugin,
    private vararg val tables: Table
) : SuspendingModule(identifier, plugin) {
    private var scope: CoroutineScope = CoroutineScope(context = plugin.minecraftDispatcher)
    protected val mutex = Mutex()

    private val database by lazy {
        val db = Database.connect("jdbc:sqlite:${plugin.dataFolder}/$databaseName.db", "org.sqlite.JDBC").apply {
            TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        }
        if (!File(plugin.dataFolder, "$databaseName.db").exists()) {
            transaction {
                SchemaUtils.createDatabase(databaseName)
            }
        }

        return@lazy db
    }

    override suspend fun suspendLoad() {
        if (!scope.isActive) {
            scope = CoroutineScope(context = plugin.minecraftDispatcher)
        }
        I.log(Level.INFO, "Database '${database.name} has been successfully connected!'")
        withContext(context = Dispatchers.IO) {
            write {
                SchemaUtils.createMissingTablesAndColumns(*tables)
            }
        }
    }

    override suspend fun suspendCancel() {
        if (scope.isActive) {
            scope.cancel()
        }
    }

    /**
     * Queue a read transaction on this database which is executed on a different thread
     */
    fun queueRead(block: Transaction.() -> Unit) {
        scope.launch(Dispatchers.IO) {
            transaction {
                try {
                    block()
                } catch (exception: SQLiteException) {
                    I.log(Level.SEVERE, "Encountered database exception!", exception)
                }
            }
        }
    }

    /**
     * Execute a read transaction on this database suspending the current thread
     */
    suspend fun read(block: Transaction.() -> Unit) = coroutineScope {
        if (scope.isActive) {
            transaction {
                try {
                    block()
                } catch (exception: SQLiteException) {
                    I.log(Level.SEVERE, "Encountered database exception!", exception)
                }
            }
        } else {
            warn("Could not read from database as database scope is no longer active!")
        }

    }

    /**
     * Queue a write transaction to this database to be executed when able
     */
    fun queueWrite(block: Transaction.() -> Unit) {
        scope.launch(Dispatchers.IO) {
            mutex.withLock {
                transaction {
                    try {
                        block()
                    } catch (exception: SQLiteException) {
                        I.log(Level.SEVERE, "Encountered database exception!", exception)
                    }
                }
            }
        }
    }


    /**
     * Execute a write transaction to this database suspending the current thread.
     */
    suspend fun write(block: Transaction.() -> Unit) = coroutineScope {
        if (scope.isActive) {
            mutex.withLock {
                transaction {
                    try {
                        block()
                    } catch (exception: SQLiteException) {
                        I.log(Level.SEVERE, "Encountered database exception!", exception)
                    }
                }
            }
        } else {
            warn("Could not write to database as database scope is no longer active!")
        }
    }
}