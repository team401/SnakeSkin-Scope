package org.snakeskin.scope.client.plot

import javafx.scene.canvas.Canvas
import org.snakeskin.scope.client.DrawingContext
import kotlin.math.ceil
import kotlin.math.max

/**
 * Plot with one or more associated numeric channels.
 */
class NumericPlot: IScopePlot {
    var min = -1.0
    var max = 12.0
    var numVerticalDivisions = 15 //Number of horizontal divisions in addition to the bottom and top

    override val backgroundCanvas = Canvas()
    override val plotCanvas = Canvas()

    private var lastContext = DrawingContext()

    override fun render(context: DrawingContext) {
        lastContext = context //Cache the context (for resize redrawing)
    }

    private var bgX = 0.0
    private var plotX = 0.0
    private var y = 0.0
    private var plotWidth = 0.0
    private var height = 0.0
    private var numTimebaseDivisions = 0

    override fun drawBackground(
        bgX: Double,
        plotX: Double,
        y: Double,
        plotWidth: Double,
        height: Double,
        numTimebaseDivisions: Int
    ) {
        //Store new values
        this.bgX = bgX
        this.plotX = plotX
        this.y = y
        this.plotWidth = plotWidth
        this.height = height
        this.numTimebaseDivisions = numTimebaseDivisions

        //Clear the existing background and foreground
        clearBackground()

        //Relocate and resize canvases
        backgroundCanvas.translateX = bgX
        backgroundCanvas.translateY = y
        plotCanvas.translateX = plotX
        plotCanvas.translateY = y
        backgroundCanvas.width = plotWidth + (plotX - bgX)
        backgroundCanvas.height = height
        plotCanvas.width = plotWidth
        plotCanvas.height = height

        //Redraw
        drawBackgroundCached()
    }

    override fun clearBackground() {
        backgroundCanvas.graphicsContext2D.clearRect(0.0, 0.0, backgroundCanvas.width, backgroundCanvas.height)
    }

    override fun clearForeground() {
        plotCanvas.graphicsContext2D.clearRect(0.0, 0.0, plotCanvas.width, plotCanvas.height)
    }

    /**
     * Draws the background with the stored parameters.  It is expected that "clearBackground" has already been called.
     *
     * This function does not resize or relocate the background, so those parameters must not be changed between calls
     */
    private fun drawBackgroundCached() {
        val heightPerDivision = backgroundCanvas.height / (numVerticalDivisions + 1)
        val widthPerDivision = plotWidth / (numTimebaseDivisions + 1)

        //Calculate number of horizontal dots based on number of divisions on the timebase
        val numHorizontalDots = (numTimebaseDivisions + 1) * max(ceil(widthPerDivision / 8.0), 3.0) //15 dots per division
        val numTimebaseDots = (numVerticalDivisions + 1) * max(ceil(heightPerDivision / 10.0), 3.0)

        DrawingUtils.drawPlotAndTimebase(
            backgroundCanvas.graphicsContext2D,
            backgroundCanvas.width,
            backgroundCanvas.height,
            plotX,
            numTimebaseDivisions,
            numTimebaseDots,
            0.5
        )

        DrawingUtils.drawVerticalNumeric(
            backgroundCanvas.graphicsContext2D,
            backgroundCanvas.width,
            backgroundCanvas.height,
            plotX,
            numVerticalDivisions,
            numHorizontalDots,
            0.5,
            min,
            max
        )
    }
}