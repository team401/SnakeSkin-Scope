package org.snakeskin.scope.client.plot

import javafx.geometry.VPos
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import org.snakeskin.scope.client.DrawingContext

class TimebaseCanvas: Canvas() {
    private var curCtx = DrawingContext()
    private var plotStart = 0.0
    private var plotWidth = 0.0

    /**
     * Relocates and then redraws the timebase
     */
    fun relocateRedraw(y: Double, width: Double, height: Double, plotStart: Double, plotWidth: Double) {
        translateY = y
        this.width = width
        this.height = height
        this.plotStart = plotStart
        this.plotWidth = plotWidth

        redrawCached() //Redraw from the last known context
    }

    fun clear() {
        graphicsContext2D.clearRect(0.0, 0.0, width, height)
    }

    fun redraw(ctx: DrawingContext) {
        curCtx = ctx
        clear()
        redrawCached()
    }

    private fun redrawCached() {
        val gc = graphicsContext2D
        val widthPerDivision = plotWidth / (curCtx.numTimebaseDivisions + 1)

        //Draw lines coming off of each timebase division
        gc.stroke = Color.WHITE
        for (i in 0..curCtx.numTimebaseDivisions) {
            val lineX = plotStart + (i * widthPerDivision)
            gc.strokeLine(lineX, 10.0, lineX, 20.0)
        }
        //Last line needs to be left shifted one pixel
        val lastLineX = plotStart + ((curCtx.numTimebaseDivisions + 1) * widthPerDivision) - 1.0
        gc.strokeLine(lastLineX, 10.0, lastLineX, 20.0)

        gc.fill = Color.WHITE
        gc.font = Font.font(12.0)
        gc.textBaseline = VPos.TOP
        gc.textAlign = TextAlignment.CENTER

        //Iterate expected timebase
        var curTimebase = curCtx.timebaseFirst
        for (i in 0..curCtx.numTimebaseDivisions) {
            val textX = plotStart + (i * widthPerDivision)
            val text = DrawingUtils.timmebaseNumberFormatter.format(curTimebase)
            gc.fillText(text, textX, 20.0)
            curTimebase += curCtx.timebaseSecondsPerDivision
        }

        val lastTextX = plotStart + ((curCtx.numTimebaseDivisions + 1) * widthPerDivision) - 1.0
        val lastText = DrawingUtils.timmebaseNumberFormatter.format(curCtx.timebaseLast)
        gc.fillText(lastText, lastTextX, 20.0)

        gc.font = Font.font(14.0)
        gc.textBaseline = VPos.CENTER
        gc.fillText("Time (Seconds)", plotStart + (plotWidth / 2.0), 50.0)
    }

}