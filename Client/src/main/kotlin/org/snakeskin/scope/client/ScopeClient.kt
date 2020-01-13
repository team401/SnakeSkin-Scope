package org.snakeskin.scope.client

import javafx.application.Application
import javafx.beans.InvalidationListener
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.*
import javafx.stage.Stage
import org.snakeskin.scope.client.plot.PlotManager
import org.snakeskin.scope.protocol.ScopeProtocol
import org.snakeskin.scope.protocol.channel.ScopeChannelBoolean
import org.snakeskin.scope.protocol.channel.ScopeChannelNumeric
import kotlin.math.sin

class ScopeClient: Application() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            System.setProperty("javafx.preloader", ScopeClientPreloader::class.java.canonicalName)
            launch(ScopeClient::class.java)
        }
    }

    override fun init() {
        Thread.sleep(500)
    }

    override fun start(stage: Stage) {
        //Load the application icon and set the title of the main window
        val iconLocation = javaClass.classLoader.getResourceAsStream("scope_icon.png")
        val icon = Image(iconLocation)
        stage.icons.add(icon)
        stage.title = "SnakeSkin Scope"

        //Load the main window
        ScopeLayoutManager.init()
        stage.scene = ScopeLayoutManager.scene
        stage.minWidth = ScopeLayoutManager.minWidth
        stage.minHeight = ScopeLayoutManager.minHeight

        val channel = ScopeChannelNumeric("Test")
        val protoLocal = ScopeProtocol(5800, listOf(channel))

        ScopeFrontend.acceptProtocol(ScopeProtocol.deserializeProtocol(protoLocal.serializeProtocol()))

        var count = 0.0

        Thread {
            while (true) {
                channel.update(sin(count))
                protoLocal.populateBuffer(count, ScopeFrontend.incomingBuffer)
                ScopeFrontend.acceptData()
                count += .01
                Thread.sleep(10)
            }
        }.apply { isDaemon = true }.start()

        //Startup
        PlotManager.start()
        stage.show()
    }
}