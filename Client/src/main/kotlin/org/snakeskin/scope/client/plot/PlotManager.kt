package org.snakeskin.scope.client.plot

import javafx.animation.AnimationTimer
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.canvas.Canvas
import javafx.scene.layout.GridPane
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import org.snakeskin.scope.client.DRAW_LOCK
import org.snakeskin.scope.client.DrawingContext
import org.snakeskin.scope.client.ScopeFrontend

/**
 * Manages and renders active plots
 */
object PlotManager: AnimationTimer() {
    //Sizing parameters for a plot.  //TODO move these into a settings manager of some sort
    object SizingParameters {
        val leftGutterPixels = 100.0 //Size of the left gutter, where vertical axis labels are drawn
        val rightPlotGutterPixels = 10.0 //Size of the gutter between the last timebase division and the end of the plot
        val rightPaddingPixels = 30.0 //Amount of padding between the end of the plot and the end of the canvas
        val bottomGutterPixels = 100.0 //Amount of gutter between the bottom padding and the end of the canvas

        val topPlotPaddingPixels = 10.0 //Amount of space to add above each plot
    }

    /**
     * List of plots.  Modify only under draw lock.
     */
    val plots: ObservableList<IScopePlot> = FXCollections.observableArrayList<IScopePlot>()
    val root = StackPane()

    private var needsRelocate = false //Flag for if relocation is required
    private var needsResize = false //Flag for if resizing is required

    private var resizeWidth = 0.0
    private var resizeHeight = 0.0

    private val timebaseCanvas = Canvas() //Canvas for drawing the canvas

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
            plots[i].drawBackground(0.0, plotOriginX, plotY, usableWidth, heightPerPlot - SizingParameters.topPlotPaddingPixels, 9)
        }

        //Relocate the timebase
        val timebaseOriginY = viewportHeight - SizingParameters.bottomGutterPixels

    }

    /**
     * Rendering function called by the animation timer.  Used to update all plots
     */
    override fun handle(now: Long) {
        DRAW_LOCK.lock() //Acquire the draw lock
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
        //ScopeFrontend.updateDraw(ctx) //Get values from the frontend

        //Render each plot
        plots.forEach {
            //it.render(ctx)
        }

        DRAW_LOCK.unlock() //Release the draw lock
    }
}