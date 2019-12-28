package org.snakeskin.scope.client

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.locks.ReentrantLock

val PARAM_EXECUTOR: ExecutorService = Executors.newSingleThreadExecutor()
val DRAW_LOCK = ReentrantLock()

/**
 * Convenience function to update parameters under the draw lock on the param executor
 */
inline fun paramUpdate(crossinline action: () -> Unit) {
    PARAM_EXECUTOR.submit {
        DRAW_LOCK.lock()
        action()
        DRAW_LOCK.unlock()
    }
}