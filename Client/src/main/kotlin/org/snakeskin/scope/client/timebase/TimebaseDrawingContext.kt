package org.snakeskin.scope.client.timebase

import javafx.scene.paint.Color
import org.snakeskin.scope.protocol.channel.ScopeChannel

class TimebaseDrawingContext {
    /**
     * Ordered 2D list of active channel indices.  The outer array corresponds to a given plot,
     * while the inner array corresponds to a channel being drawn within that plot
     */
    val activeChannels = ArrayList<ArrayList<Pair<Int, Color>>>()

    /**
     * Last index of data in the buffers
     */
    var lastDataIndex = 0

    /**
     * Whether or not the scope is currently streaming in data
     */
    var running = false
}