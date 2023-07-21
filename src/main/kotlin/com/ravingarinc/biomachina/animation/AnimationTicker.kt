package com.ravingarinc.biomachina.animation

import com.github.shynixn.mccoroutine.bukkit.ticks
import com.ravingarinc.api.module.RavinPlugin
import com.ravingarinc.biomachina.api.Ticker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore

class AnimationTicker(plugin: RavinPlugin, threads: Int, private val controllers: MutableCollection<AnimationController<*>>) : Ticker(plugin, 1.ticks) {
    private val semaphore: Semaphore = Semaphore(threads)
    override suspend fun CoroutineScope.tick() {
        ArrayList(controllers).forEach {
            semaphore.acquire()
            scope.launch {
                try {
                    if(!it.isDisposed()) it.tick()
                }
                finally {
                    semaphore.release()
                }
            }
        }
    }
}