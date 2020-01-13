package org.snakeskin.scope.client

import java.util.concurrent.locks.ReentrantLock

val DRAW_LOCK = ReentrantLock()

/**
 * Convenience function to update parameters under the draw lock on the param executor
 */
inline fun paramUpdate(crossinline action: () -> Unit) {
    DRAW_LOCK.lock()
    action()
    DRAW_LOCK.unlock()
}