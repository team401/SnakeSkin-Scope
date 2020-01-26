package org.snakeskin.scope.client

import javafx.beans.InvalidationListener
import javafx.beans.Observable
import javafx.fxml.FXMLLoader
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.layout.*
import javafx.scene.paint.Color
import org.snakeskin.scope.client.plot.PlotManager

/**
 * Hacked together layout manager that forces the grid I want.
 * JavaFX grid layout is very bad!
 */
object ScopeLayoutManager {
    private const val bottomControlGutterSize = 150.0
    private const val rightControlGutterSize = 375.0

    private val root = Pane()
    val scene = Scene(root)
    private lateinit var timebaseControls: AnchorPane
    private lateinit var mainControls: AnchorPane
    private lateinit var plotControls: AnchorPane

    const val minWidth = 640.0 + rightControlGutterSize
    const val minHeight = 480.0 + bottomControlGutterSize

    private object Listener: InvalidationListener {
        override fun invalidated(observable: Observable?) {
            val width = scene.width
            val height = scene.height

            if (!width.isNaN() && !height.isNaN()) {
                root.minWidth = width
                root.minHeight = height
                root.resize(width, height)

                PlotManager.resizeRoot(
                    width - rightControlGutterSize,
                    height - bottomControlGutterSize
                )

                val timebaseControlsX = 0.0
                val timebaseControlsY = height - bottomControlGutterSize
                val timebaseControlsWidth = width - rightControlGutterSize
                val timebaseControlsHeight = bottomControlGutterSize
                timebaseControls.minWidth = timebaseControlsWidth
                timebaseControls.minHeight = timebaseControlsHeight
                timebaseControls.maxWidth = timebaseControlsWidth
                timebaseControls.maxHeight = timebaseControlsHeight
                timebaseControls.translateX = timebaseControlsX
                timebaseControls.translateY = timebaseControlsY
                timebaseControls.resize(timebaseControlsWidth, timebaseControlsHeight)

                val mainControlsX = width - rightControlGutterSize
                val mainControlsY = height - bottomControlGutterSize
                val mainControlsWidth = rightControlGutterSize
                val mainControlsHeight = bottomControlGutterSize
                mainControls.minWidth = mainControlsWidth
                mainControls.minHeight = mainControlsHeight
                mainControls.maxWidth = mainControlsWidth
                mainControls.maxHeight = mainControlsHeight
                mainControls.translateX = mainControlsX
                mainControls.translateY = mainControlsY
                mainControls.resize(mainControlsWidth, mainControlsHeight)

                val plotControlsX = width - rightControlGutterSize
                val plotControlsY = 0.0
                val plotControlsWidth = rightControlGutterSize
                val plotControlsHeight = height - bottomControlGutterSize
                plotControls.minWidth = plotControlsWidth
                plotControls.minHeight = plotControlsHeight
                plotControls.maxWidth = plotControlsWidth
                plotControls.maxHeight = plotControlsHeight
                plotControls.translateX = plotControlsX
                plotControls.translateY = plotControlsY
                plotControls.resize(plotControlsWidth, plotControlsHeight)
            }
        }
    }

    fun init() {
        scene.widthProperty().addListener(Listener)
        scene.heightProperty().addListener(Listener)

        val timebaseControlsLoader = FXMLLoader(javaClass.classLoader.getResource("timebase_controls.fxml"))
        timebaseControls = timebaseControlsLoader.load<AnchorPane>()
        val mainControlsLoader = FXMLLoader(javaClass.classLoader.getResource("main_controls.fxml"))
        mainControls = mainControlsLoader.load<AnchorPane>()
        val plotControlsLoader = FXMLLoader(javaClass.classLoader.getResource("plot_controls.fxml"))
        plotControls = plotControlsLoader.load<AnchorPane>()

        root.children.add(PlotManager.root)
        root.children.add(timebaseControls)
        root.children.add(mainControls)
        root.children.add(plotControls)
    }
}