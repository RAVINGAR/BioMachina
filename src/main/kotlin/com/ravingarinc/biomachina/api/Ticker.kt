package com.ravingarinc.biomachina.api

import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.ravingarinc.api.I
import com.ravingarinc.api.module.RavinPlugin
import com.ravingarinc.api.module.warn
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.logging.Level
import kotlin.math.roundToLong
import kotlin.system.measureTimeMillis

abstract class Ticker(protected val plugin: RavinPlugin, interval: Int) {
    protected val scope = CoroutineScope(plugin.minecraftDispatcher)
    private val millis: Long = interval.ticks
    private val metrics: Queue<Long> = ConcurrentLinkedQueue()
    private var max: Long = 0
    fun start(delay: Int = 0) {
        if (!scope.isActive) {
            I.log(Level.WARNING, "Cannot start this ticker as it has already been used!")
        }
        scope.launch(Dispatchers.IO) {
            if (delay > 0) {
                delay(delay.ticks)
            }

            while (isActive) {
                val time = measureTimeMillis {
                    tick()
                }
                if (time > 0L) {
                    if (time > max) {
                        max = time
                    }
                    metrics.add(time)
                    if (metrics.size > 16) {
                        metrics.poll()
                    }
                }
                var next = millis - time
                if (next < 0) {
                    warn(
                        "Ticker is running ${formatMilliseconds(next * -1)} behind! Please consider " +
                                "increasing the tick interval!"
                    )
                    next = 20.ticks
                }
                delay(next)
            }
        }
    }

    fun getLastTickTime(): Long {
        return if (metrics.isEmpty()) -1 else metrics.last()
    }

    fun getAverageTickTime(): Long {
        return if (metrics.isEmpty()) -1 else ArrayList(metrics).average().roundToLong()
    }

    fun getMaxTickTime(): Long {
        return max
    }

    fun cancel() {
        if (scope.isActive) {
            scope.cancel()
        }
    }

    abstract suspend fun CoroutineScope.tick()
}