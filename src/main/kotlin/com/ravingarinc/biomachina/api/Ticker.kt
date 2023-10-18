package com.ravingarinc.biomachina.api

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.ravingarinc.api.I
import com.ravingarinc.api.module.RavinPlugin
import com.ravingarinc.api.module.warn
import kotlinx.coroutines.*
import java.util.logging.Level
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis

abstract class Ticker(protected val plugin: RavinPlugin, private val period: Long, private val context: CoroutineContext = Dispatchers.IO) {
    protected val scope = CoroutineScope(plugin.minecraftDispatcher)
    fun start(delayTicks: Int = -1) {
        if (!scope.isActive) {
            I.log(Level.WARNING, "Cannot start this ticker as it has already been used!")
        }
        scope.launch(context) {
            delay(delayTicks.ticks)
            while (isActive) {
                val time = measureTimeMillis { tick() }
                var next = period - time
                if (next < 0) {
                    warn(
                        "Ticker is running ${formatMilliseconds(next * -1)} behind! Please consider " +
                                "increasing the tick interval!"
                    )
                    next = 0
                }
                delay(next)
            }
        }
    }

    fun cancel() {
        if (scope.isActive) {
            scope.cancel()
        }
    }

    abstract suspend fun CoroutineScope.tick()
}