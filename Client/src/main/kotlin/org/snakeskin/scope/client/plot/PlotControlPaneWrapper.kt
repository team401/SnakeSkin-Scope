package org.snakeskin.scope.client.plot

import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay
import javafx.scene.control.TitledPane
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox

/**
 * Wraps a control pane in a collapsible interface
 */
class PlotControlPaneWrapper(val name: String, val controlPane: Pane): TitledPane() {
    private val deleteButton = Button("Remove Plot")

    companion object {
        private val margins = Insets(10.0, 0.0, 0.0, 0.0)
    }

    //Identify a wrapper only by its child control pane
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PlotControlPaneWrapper) return false

        if (controlPane != other.controlPane) return false

        return true
    }

    override fun hashCode(): Int {
        return controlPane.hashCode()
    }

    private fun onDelete(e: ActionEvent) {
        //Delete the plot from the plot manager
        PlotManager.removeFromControlPane(this)
    }

    init {
        deleteButton.onAction = EventHandler(::onDelete)

        VBox.setMargin(this, margins)
        text = name
        contentDisplay = ContentDisplay.RIGHT //Display buttons on the right
        graphicTextGap = 20.0
        graphic = deleteButton //Show delete button
        content = controlPane
    }
}