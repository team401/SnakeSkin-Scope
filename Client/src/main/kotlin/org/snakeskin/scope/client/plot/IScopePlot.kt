package org.snakeskin.scope.client.plot

import javafx.scene.canvas.Canvas
import javafx.scene.layout.Pane

/**
 * Interface for a general scope plot
 */
interface IScopePlot {
    /**
     * Render the plot given the drawing context
     */
    fun render(context: PlotDrawingContext)

    /**
     * Draws the background of the plot
     */
    fun drawBackground(
        bgX: Double,
        plotX: Double,
        y: Double,
        plotWidth: Double,
        height: Double,
        numTimebaseDivisions: Int)

    /**
     * Clears the background of the plot
     */
    fun clearBackground()

    /**
     * Clears the foreground of the plot
     */
    fun clearForeground()

    /**
     * Reads and updates all settings for the plot
     */
    fun updateSettings()

    /**
     * Canvas for the background of the plot.  This includes the outline, divisions, and vertical axis labels
     */
    val backgroundCanvas: Canvas

    /**
     * Canvas for the plot itself.  This is where the actual trace is drawn.
     */
    val plotCanvas: Canvas

    /**
     * Control pane for the plot.  This pane is displayed on the righthand side of the screen.
     */
    val controlPane: Pane
}