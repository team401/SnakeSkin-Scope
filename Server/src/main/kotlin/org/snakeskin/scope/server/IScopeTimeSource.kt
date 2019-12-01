package org.snakeskin.scope.server

/**
 * A time source for the scope.  Subclasses should return a relative time, in seconds
 * obtained as accurately as possible.  This time does not need to correlate with "wall clock time"
 * in any way, but instead just needs to be a relative time source that increments accurately.
 */
interface IScopeTimeSource {
    fun getTimeNow(): Double
}