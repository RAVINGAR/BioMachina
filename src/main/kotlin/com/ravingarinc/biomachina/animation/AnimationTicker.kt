package com.ravingarinc.biomachina.animation

import com.ravingarinc.api.module.RavinPlugin
import com.ravingarinc.biomachina.api.Ticker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import java.util.*

class AnimationTicker(plugin: RavinPlugin, threads: Int, private val controllers: MutableCollection<AnimationController<*>>) : Ticker(plugin, 50) {
    private val semaphore: Semaphore = Semaphore(threads)
    override suspend fun CoroutineScope.tick() {
        LinkedList(controllers).forEach {
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