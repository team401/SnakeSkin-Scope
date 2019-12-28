package org.snakeskin.scope.client.data

import javafx.scene.paint.Color
import org.snakeskin.scope.client.ScopeFrontend
import org.snakeskin.scope.client.buffer.ChannelBuffer
import org.snakeskin.scope.client.buffer.NumericChannelBuffer
import org.snakeskin.scope.client.paramUpdate
import org.snakeskin.scope.protocol.channel.ScopeChannelNumeric

/**
 * Scope trace that renders one or more numeric channels onto a single plot.
 * Can also render a single boolean channel as an "overlay".
 */
class NumericGroupTrace {
    companion object {
        /**
         * Default colors to use for scope traces
         */
        private val defaultColors = arrayOf(
            Color.YELLOW,
            Color.CYAN,
            Color.MAGENTA,
            Color.LIME,
            Color.RED,
            Color.BLUE
        )
    }

    private var colorSelector = 0

    private fun takeColor(): Color {
        val color = defaultColors[colorSelector]
        colorSelector++
        colorSelector %= defaultColors.size
        return color
    }

    /**
     * Channels array.  Modifications to this should be made under the draw lock on the param executor
     */
    private val traces = arrayListOf<Pair<NumericChannelBuffer, Color>>()

    /**
     * Adds a new channel to the list
     */
    fun addChannel(channel: Int) = paramUpdate {
        val buffer = ScopeFrontend.channelBuffers[channel] //Get the channel buffer from the frontend
        if (buffer is NumericChannelBuffer) {
            //Only add the channel if it's a numeric channel
            val color = takeColor() //Get a color for the trace
            traces.add(buffer to color)
        }
    }

    fun updateChannelColor(idx: Int) = paramUpdate {

    }
}