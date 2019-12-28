package org.snakeskin.scope.client.plot

import javafx.scene.canvas.Canvas
import org.snakeskin.scope.client.DrawingContext

class TimebaseCanvas: Canvas() {
    private var curCtx = DrawingContext()

    /**
     * Relocates and then redraws the timebase
     */
    fun relocateRedraw(y: Double, width: Double, height: Double) {
        translateX = 0.0
        translateY = y
        this.width = width
        this.height = height

        redrawCached() //Redraw from the last known context
    }

    fun clear() {
        graphicsContext2D.clearRect(0.0, 0.0, width, height)
    }

    fun redraw(ctx: DrawingContext) {
        curCtx = ctx
        redrawCached()
    }

    private fun redrawCached() {

    }
}