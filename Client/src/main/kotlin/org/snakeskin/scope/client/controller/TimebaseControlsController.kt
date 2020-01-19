package org.snakeskin.scope.client.controller

import javafx.beans.InvalidationListener
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.*
import javafx.scene.paint.Color
import org.snakeskin.scope.client.plot.PlotManager
import org.snakeskin.scope.client.timebase.TimebaseBackgroundCanvas
import org.snakeskin.scope.client.timebase.TimebaseManager

class TimebaseControlsController {
    @FXML lateinit var rootAnchor: AnchorPane
    @FXML lateinit var timebaseAnchor: AnchorPane

    private val root = StackPane()
    private val backgroundCanvas = TimebaseManager.backgroundCanvas

    fun initialize() {
        AnchorPane.setLeftAnchor(root, 0.0)
        AnchorPane.setRightAnchor(root, 0.0)
        AnchorPane.setTopAnchor(root, 0.0)
        AnchorPane.setBottomAnchor(root, 0.0)
        timebaseAnchor.children.add(root)

        root.background = Background(BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY))
        StackPane.setAlignment(backgroundCanvas, Pos.TOP_LEFT)
        root.children.add(backgroundCanvas)

        rootAnchor.widthProperty().addListener { _, _, newValue ->
            backgroundCanvas.clear()
            backgroundCanvas.resizeRedraw(newValue.toDouble(), root.height)
        }
    }
}