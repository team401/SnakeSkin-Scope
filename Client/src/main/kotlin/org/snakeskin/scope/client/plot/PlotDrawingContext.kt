package org.snakeskin.scope.client.plot

import org.snakeskin.scope.client.buffer.TimestampBuffer

/**
 * Drawing context class.  Gets populated by the frontend before drawing
 */
class PlotDrawingContext {
    /**
     * First index of data to draw
     */
    var firstIndex = 0

    /**
     * Last index of data to draw
     */
    var lastIndex = 0

    /**
     * First timebase value to draw
     */
    var timebaseFirst = 0.0

    /**
     * Last timebase value to draw
     */
    var timebaseLast = 0.0

    /**
     * Number of divisions on the timebase
     */
    var numTimebaseDivisions = 0

    /**
     * Seconds per timebase division
     */
    var timebaseSecondsPerDivision = 0.0
}