package org.snakeskin.scope.client.data

/**
 * A scope trace.
 */
interface IScopeTrace {
    /**
     * The minimum for the vertical axis of this trace
     */
    val min: Double

    /**
     * The maximum for the vertical axis of this trace
     */
    val max: Double

    /**
     * The number of graduations to put on this trace
     */
    val graduations: Double
}