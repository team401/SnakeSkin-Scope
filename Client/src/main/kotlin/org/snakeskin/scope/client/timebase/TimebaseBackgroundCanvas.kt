package org.snakeskin.scope.client.timebase

import javafx.geometry.VPos
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import org.snakeskin.scope.client.ScopeFrontend
import org.snakeskin.scope.client.buffer.NumericChannelBuffer
import org.snakeskin.scope.client.plot.DrawingUtils

class TimebaseBackgroundCanvas: Canvas() {
    private val padding = 10.0
    private var curCtx = TimebaseDrawingContext()

    fun resizeRedraw(width: Double, height: Double) {
        this.width = width
        this.height = height
        redrawCached()
    }

    fun clear() {
        graphicsContext2D.clearRect(0.0, 0.0, width, height)
    }

    fun redraw(ctx: TimebaseDrawingContext) {
        this.curCtx = ctx
        redrawCached()
    }

    private fun drawPoint(yOffset: Double, t: Double, y: Double, tPrev: Double, yPrev: Double, tLast: Double, yMin: Double, yMax: Double, width: Double, height: Double) {
        val tPixels = DrawingUtils.PixelMath.calcX(t, 0.0, tLast, width) + padding //Shift right by padding
        val tPrevPixels = DrawingUtils.PixelMath.calcX(tPrev, 0.0, tLast, width) + padding

        val yPixels = DrawingUtils.PixelMath.calcY(y, yMin, yMax, height) + yOffset //Shift down by y offset
        val yPrevPixels = DrawingUtils.PixelMath.calcY(yPrev, yMin, yMax, height) + yOffset

        graphicsContext2D.strokeLine(tPrevPixels, yPrevPixels, tPixels, yPixels)
    }

    private fun redrawCached() {
        val gc = graphicsContext2D
        if (curCtx.running) {
            //While running, simply draw some text indicating that the scope is running
            gc.fill = Color.WHITE
            gc.font = Font.font(14.0)
            gc.textAlign = TextAlignment.CENTER
            gc.textBaseline = VPos.CENTER
            gc.fillText("Running", width / 2.0, height / 2.0)
            return
        }

        //Determine the height to use per division
        val usableHeight = height - (2.0 * padding)
        val usableWidth = width - (2.0 * padding)
        val heightPerPlot = usableHeight / curCtx.activeChannels.size //Number of pixels to use per plot

        var activeTop = padding

        val tArr = ScopeFrontend.timestampBuffer.arr
        val tLast = tArr[curCtx.lastDataIndex] //Get the last timestamp

        curCtx.activeChannels.forEach {
            plot ->
            //Draw each channel on the plot
            plot.forEach {
                val (idx, color) = it
                when (val buf = ScopeFrontend.channelBuffers[idx]) {
                    is NumericChannelBuffer -> {
                        var minValue = 0.0
                        var maxValue = 0.0
                        for (i in 0..curCtx.lastDataIndex) {
                            //Find the lowest and highest values
                            val value = buf.arr[i]
                            if (value > maxValue) maxValue = value
                            if (value < minValue) minValue = value
                        }

                        //Draw data
                        gc.stroke = color
                        for (i in 1..curCtx.lastDataIndex) {
                            drawPoint(
                                activeTop,
                                tArr[i],
                                buf.arr[i],
                                tArr[i - 1],
                                buf.arr[i - 1],
                                tLast,
                                minValue,
                                maxValue,
                                usableWidth,
                                heightPerPlot
                            )
                        }
                    }
                }
            }

            activeTop += heightPerPlot
        }
    }
}