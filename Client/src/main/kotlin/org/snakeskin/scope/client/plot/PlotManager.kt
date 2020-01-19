package org.snakeskin.scope.client.plot

import javafx.animation.AnimationTimer
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.*
import javafx.scene.paint.Color
import org.snakeskin.scope.client.DRAW_LOCK
import org.snakeskin.scope.client.ScopeFrontend
import org.snakeskin.scope.client.timebase.TimebaseDrawingContext

/**
 * Manages and renders active plots
 */
object PlotManager: AnimationTimer() {
    //Sizing parameters for a plot.  //TODO move these into a settings manager of some sort
    object SizingParameters {
        val leftGutterPixels = 100.0 //Size of the left gutter, where vertical axis labels are drawn
        val rightPlotGutterPixels = 10.0 //Size of the gutter between the last timebase division and the end of the plot
        val rightPaddingPixels = 30.0 //Amount of padding between the end of the plot and the end of the canvas
        val bottomGutterPixels = 75.0 //Amount of gutter between the bottom padding and the end of the canvas

        val topPlotPaddingPixels = 10.0 //Amount of space to add above each plot
    }

    /**
     * List of plots.  Modify only under draw lock.
     */
    val plots: ObservableList<IScopePlot> = FXCollections.observableArrayList<IScopePlot>()
    val root = StackPane()

    private val ctx = PlotDrawingContext()

    private var needsRelocate = false //Flag for if relocation is required
    private var needsResize = false //Flag for if resizing is required

    private var resizeWidth = 0.0
    private var resizeHeight = 0.0

    private val timebaseCanvas = PlotTimebaseCanvas() //Canvas for drawing the timebase

    private var lastNumTimebaseDivisions = 0 //Used to decide whether to relocate when the number of divisions changes

    init {
        root.background = Background(BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY))
        StackPane.setAlignment(timebaseCanvas, Pos.TOP_LEFT)
        root.children.add(timebaseCanvas)
    }

    /**
     * Adds a new plot.  Call this method from the FX thread
     */
    fun addPlot(plot: IScopePlot) {
        DRAW_LOCK.lock()
        plots.add(plot)
        //When a plot is added, its canvases need to be added to the children of the root StackPane
        StackPane.setAlignment(plot.backgroundCanvas, Pos.TOP_LEFT) //Translate off of the top left corner
        StackPane.setAlignment(plot.plotCanvas, Pos.TOP_LEFT)
        root.children.addAll(plot.backgroundCanvas, plot.plotCanvas)
        needsRelocate = true //Mark for relocation
        DRAW_LOCK.unlock()
    }

    /**
     * Removes a plot.  Call this method from the FX thread
     */
    fun removePlot(plot: IScopePlot) {
        DRAW_LOCK.lock()
        plots.remove(plot)
        //When a plot is removed, its canvases need to be removed from the children of the root StackPane
        root.children.removeAll(plot.backgroundCanvas, plot.plotCanvas)
        needsRelocate = true //Mark for relocation
        DRAW_LOCK.unlock()
    }

    /**
     * Removes a plot via the reference of the control frame
     */
    fun removeFromControlPane(pane: PlotControlPaneWrapper) {
        DRAW_LOCK.lock()
        val plot = plots.first { it.controlPane == pane.controlPane }
        removePlot(plot)
        DRAW_LOCK.unlock()
    }

    fun resizeRoot(width: Double, height: Double) {
        DRAW_LOCK.lock()
        resizeWidth = width
        resizeHeight = height
        needsResize = true
        DRAW_LOCK.unlock()
    }

    private fun relocatePlots() {
        val viewportWidth = root.width //Get the current dimensions of the viewport
        val viewportHeight = root.height

        val numPlots = plots.size //Get current number of plots

        val usableHeight = viewportHeight - SizingParameters.bottomGutterPixels
        val usableWidth = viewportWidth - SizingParameters.leftGutterPixels - SizingParameters.rightPaddingPixels

        val plotOriginX = SizingParameters.leftGutterPixels //Start plots at the end of the gutter
        val heightPerPlot = usableHeight / numPlots

        for (i in 0 until numPlots) {
            val plotY = (i * heightPerPlot) + SizingParameters.topPlotPaddingPixels //Calculate y coordinate for each plot
            plots[i].clearBackground() //Clear each plot
            plots[i].clearForeground()
            plots[i].drawBackground(
                0.0,
                plotOriginX,
                plotY,
                usableWidth,
                heightPerPlot - SizingParameters.topPlotPaddingPixels,
                ctx.numTimebaseDivisions
            )
        }

        //Relocate the timebase
        val timebaseOriginY = viewportHeight - SizingParameters.bottomGutterPixels
        val timebaseHeight = SizingParameters.bottomGutterPixels

        timebaseCanvas.clear()
        timebaseCanvas.relocateRedraw(timebaseOriginY, viewportWidth, timebaseHeight, plotOriginX, usableWidth)
    }

    /**
     * Updates the provided timebase context the active channels
     */
    fun updateTimebase(ctx: TimebaseDrawingContext) {
        DRAW_LOCK.lock()
        ctx.activeChannels.clear()
        plots.forEach {
            val subList = arrayListOf<Pair<Int, Color>>()
            when (it) {
                is NumericPlot -> subList.addAll(it.channelIndices)
            }
            ctx.activeChannels.add(subList)
        }
        DRAW_LOCK.unlock()
    }

    /**
     * Rendering function called by the animation timer.  Used to update all plots
     */
    override fun handle(now: Long) {
        DRAW_LOCK.lock() //Acquire the draw lock
        ScopeFrontend.updateDraw(ctx) //Get latest values from the frontend

        val numPlots = plots.size

        if (ctx.numTimebaseDivisions != lastNumTimebaseDivisions) {
            lastNumTimebaseDivisions = ctx.numTimebaseDivisions
            needsRelocate = true
        }

        if (needsResize) {
            root.minWidth = resizeWidth
            root.minHeight = resizeHeight
            root.resize(resizeWidth, resizeHeight)
            needsResize = false
            needsRelocate = true //Always relocate after a resize
        }

        if (needsRelocate) {
            relocatePlots() //Relocate plots if we have to.  This guarantees that the operation is done in the FX thread
            needsRelocate = false
        }

        //Update timebase
        timebaseCanvas.redraw(ctx, numPlots > 0)

        //Render each plot
        plots.forEach {
            it.clearForeground()
            it.render(ctx)
        }

        DRAW_LOCK.unlock() //Release the draw lock
    }
}