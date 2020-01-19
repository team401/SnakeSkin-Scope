package org.snakeskin.scope.client.plot

import javafx.geometry.Insets
import javafx.scene.canvas.Canvas
import javafx.scene.control.Label
import javafx.scene.control.Spinner
import javafx.scene.layout.*
import javafx.scene.paint.Color
import org.snakeskin.scope.client.ScopeFrontend
import org.snakeskin.scope.client.buffer.NumericChannelBuffer
import org.snakeskin.scope.client.util.NumericTextField
import org.snakeskin.scope.protocol.channel.ScopeChannelNumeric
import kotlin.math.ceil
import kotlin.math.max

/**
 * Plot with one or more associated numeric channels.
 */
class NumericPlot: IScopePlot {
    companion object {
        private val controlMargins = Insets(0.0, 10.0, 0.0, 10.0)
        private val controlHboxPadding = Insets(0.0, 10.0, 10.0, 10.0)
        private val channelsVboxPadding = Insets(10.0, 10.0, 10.0, 10.0)
        private val controlLabelPadding = Insets(5.0, 0.0, 0.0, 0.0)
    }

    var min = -20.0
    var max = 20.0
    var numVerticalDivisions = 3 //Number of horizontal divisions in addition to the bottom and top

    /**
     * Active channel indices.  It is assumed that these indices correspond with a numeric channel type
     */
    val channelIndices = arrayListOf<Pair<Int, Color>>()

    init {
        channelIndices.add(0 to Color.YELLOW)
    }

    /**
     * Control pane for a numeric plot
     */
    inner class ControlPane: BorderPane() {
        private val minField = NumericTextField(min)
        private val maxField = NumericTextField(max)
        private val divsSpinner = Spinner<Int>(0, Int.MAX_VALUE, numVerticalDivisions)

        private val channelsVbox = VBox()
        private val controlsHbox = HBox(
            Label("Min:").apply { padding = controlLabelPadding },
            minField,
            Label("Max:").apply { padding = controlLabelPadding },
            maxField,
            Label("Divs:").apply { padding = controlLabelPadding },
            divsSpinner
        )



        init {
            minField.prefWidth = 50.0
            maxField.prefWidth = 50.0
            divsSpinner.prefWidth = 50.0

            HBox.setMargin(minField, controlMargins)
            HBox.setMargin(maxField, controlMargins)
            HBox.setMargin(divsSpinner, controlMargins)

            controlsHbox.padding = controlHboxPadding

            bottom = controlsHbox

            minField.numberProperty().addListener { _, _, newValue ->
                min = newValue.toDouble()
                clearBackground()
                drawBackgroundCached()
            }

            maxField.numberProperty().addListener { _, _, newValue ->
                max = newValue.toDouble()
                clearBackground()
                drawBackgroundCached()
            }

            divsSpinner.valueProperty().addListener { _, _, newValue ->
                numVerticalDivisions = newValue
                clearBackground()
                drawBackgroundCached()
            }
        }
    }

    override val backgroundCanvas = Canvas()
    override val plotCanvas = Canvas()

    override val controlPane = ControlPane()

    private var lastContext = PlotDrawingContext()

    private val timestampBuffer = ScopeFrontend.timestampBuffer
    private val channelBuffer by lazy { ScopeFrontend.channelBuffers[0] as NumericChannelBuffer }

    private fun getXLocation(x: Double): Double {
        val widthPerDivision = plotWidth / (numTimebaseDivisions + 1)
        return (x - lastContext.timebaseFirst) / (lastContext.timebaseLast - lastContext.timebaseFirst) * (plotWidth)
    }

    private fun getYLocation(y: Double): Double {
        return (max - y) / (max - min) * height
    }

    private fun plotPoint(t: Double, y: Double, tLast: Double, yLast: Double, color: Color) {
        //Convert points to pixel locations
        val widthPerDivision = plotWidth / (numTimebaseDivisions + 1)
        val yPosPixels = (max - y) / (max - min) * height //Distance from max to point over range is a percent, multiply to get pixels
        //Same as above, only multiplying by plot width minus one division width because the last division is fake, and also offsetting by the x offset
        val tPosPixels = (t - lastContext.timebaseFirst) / (lastContext.timebaseLast - lastContext.timebaseFirst) * (plotWidth)

        val yLastPostPixels = (max - yLast) / (max - min) * height
        val tLastPosPixels = (tLast - lastContext.timebaseFirst) / (lastContext.timebaseLast - lastContext.timebaseFirst) * (plotWidth)

        plotCanvas.graphicsContext2D.stroke = color
        plotCanvas.graphicsContext2D.strokeLine(tLastPosPixels, yLastPostPixels, tPosPixels, yPosPixels)
    }

    var fillColor = Color(0.0, 0.5, 0.0, 0.25)

    override fun updateSettings() {

    }

    override fun render(context: PlotDrawingContext) {
        lastContext = context //Cache the context (for resize redrawing)

        for (i in (context.firstIndex + 1)..context.lastIndex) {
            //Render the points
            val lastTime = timestampBuffer.arr[i - 1]
            val lastPoint = channelBuffer.arr[i - 1]
            val time = timestampBuffer.arr[i]
            val point = channelBuffer.arr[i]
            plotPoint(time, point, lastTime, lastPoint, Color.YELLOW)
        }

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