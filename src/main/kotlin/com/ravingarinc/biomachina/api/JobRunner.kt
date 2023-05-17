package com.ravingarinc.biomachina.api

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.ravingarinc.api.module.RavinPlugin
import com.ravingarinc.api.module.severe
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import kotlin.coroutines.CoroutineContext

open class JobRunner(plugin: RavinPlugin, val threadCount: Int = 1) {
    private val scope: CoroutineScope = CoroutineScope(plugin.minecraftDispatcher)
    private val semaphore: Semaphore = Semaphore(threadCount)
    private val queue: BlockingQueue<RunnableJob<out Any>> = LinkedBlockingQueue()
    val size: Int
        get() = queue.size + threadCount - semaphore.availablePermits

    val threadsInUse: Int
        get() = threadCount - semaphore.availablePermits

    fun <T : Any> queue(
        context: CoroutineContext = Dispatchers.IO,
        block: suspend CoroutineScope.() -> T
    ): Deferred<T> {
        if (!scope.isActive) {
            throw IllegalStateException("Cannot queue a job as this runner is not active!")
        }
        val deferred = CompletableDeferred<T>()
        val job = RunnableJob(context, block, deferred)
        queue.add(job)
        return deferred
    }

    fun start() {
        if (!scope.isActive) {
            return
        }
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                val job = queue.take()
                semaphore.acquire()
                scope.launch(job.context) {
                    try {
                        executeJob(job)
                    } finally {
                        semaphore.release()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    protected open suspend fun executeJob(job: RunnableJob<out Any>) {
        job.complete()
        job.deferred.getCompletionExceptionOrNull()?.let {
            severe("Encountered exception in JobRunner!", it)
        }
    }

    /**
     * Cancel all queued tasks
     */
    fun cancel() {
        if (scope.isActive) {
            scope.cancel()
        }
    }

    protected class RunnableJob<T>(
        val context: CoroutineContext,
        val block: suspend CoroutineScope.() -> T,
        val deferred: CompletableDeferred<T>
    ) {
        suspend fun complete() = coroutineScope {
            deferred.completeWith(result(this))
        }

        private suspend fun result(scope: CoroutineScope): Result<T> = runCatching {
            return@runCatching block.invoke(scope)
        }
    }
}