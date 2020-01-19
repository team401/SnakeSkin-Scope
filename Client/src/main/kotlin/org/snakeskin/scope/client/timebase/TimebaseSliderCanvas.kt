package org.snakeskin.scope.client.timebase

import javafx.scene.canvas.Canvas
import org.snakeskin.scope.client.plot.PlotDrawingContext

class TimebaseSliderCanvas: Canvas() {
    private var curCtx = PlotDrawingContext()

    fun resizeRedraw(width: Double, height: Double) {
        this.width = width
        this.height = height
    }

}