package org.snakeskin.scope.client.controller

import javafx.collections.ListChangeListener
import javafx.fxml.FXML
import javafx.scene.layout.VBox
import org.snakeskin.scope.client.plot.NumericPlot
import org.snakeskin.scope.client.plot.PlotControlPaneWrapper
import org.snakeskin.scope.client.plot.PlotManager

class PlotControlsController {
    @FXML lateinit var plotListVbox: VBox

    fun initialize() {
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