package com.ravingarinc.yplants.api

import com.ravingarinc.api.module.RavinPlugin
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicLong
import kotlin.system.measureTimeMillis

class MetricJobRunner(plugin: RavinPlugin, threadCount: Int = 1) : JobRunner(plugin, threadCount) {
    private var highestTime: AtomicLong = AtomicLong(0)
    private val times = ConcurrentLinkedQueue<Long>()
    override suspend fun executeJob(job: RunnableJob<out Any>) {
        logTime(measureTimeMillis {
            super.executeJob(job)
        })
    }

    fun getHighestTime(): Long {
        return highestTime.getAcquire()
    }

    fun getAverageTime(): Long {
        return ArrayList(times).average().toLong()
    }

    fun logTime(time: Long) {
        if (time > getHighestTime()) {
            highestTime.setRelease(time)
        }
        if (times.size > threadCount) {
            times.poll()
        }
        times.add(time)
    }
}