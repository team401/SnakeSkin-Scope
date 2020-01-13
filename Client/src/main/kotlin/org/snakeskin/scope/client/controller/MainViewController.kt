package org.snakeskin.scope.client.controller

import javafx.beans.binding.Bindings
import javafx.collections.ListChangeListener
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.control.TitledPane
import javafx.scene.layout.*
import javafx.scene.paint.Color
import org.snakeskin.scope.client.ScopeClient
import org.snakeskin.scope.client.ScopeClientPreloader
import org.snakeskin.scope.client.plot.NumericPlot
import org.snakeskin.scope.client.plot.PlotControlPaneWrapper
import org.snakeskin.scope.client.plot.PlotManager

class MainViewController {
    @FXML lateinit var plotListVbox: VBox
    @FXML lateinit var plotAnchor: AnchorPane

    fun initialize() {
        //Add the plot manager root to the anchor
        AnchorPane.setLeftAnchor(PlotManager.root, 0.0)
        AnchorPane.setRightAnchor(PlotManager.root, 0.0)
        AnchorPane.setTopAnchor(PlotManager.root, 0.0)
        AnchorPane.setBottomAnchor(PlotManager.root, 0.0)
        plotAnchor.children.add(PlotManager.root)

        //Register listener with plot manager
        PlotManager.plots.addListener(ListChangeListener {
            while (it.next()) {
                //Handle removed items
                it.removed.forEach {
                    plot ->
                    //Find the wrapper of each element
                    val plotControlPane = plot.controlPane
                    val wrapper = plotListVbox.children.first { wrapper -> (wrapper as? PlotControlPaneWrapper)?.controlPane == plotControlPane }
                    plotListVbox.children.remove(wrapper)
                }

                //Handle added items
                plotListVbox.children.addAll(it.from, it.addedSubList.map { plot ->
                    PlotControlPaneWrapper("Plot", plot.controlPane)
                })
            }
        })
    }

    fun onAddNumeric() {
        PlotManager.addPlot(NumericPlot())
    }
}