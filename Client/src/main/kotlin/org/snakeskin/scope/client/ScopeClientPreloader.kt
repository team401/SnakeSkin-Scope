package org.snakeskin.scope.client

import javafx.application.Preloader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.stage.Modality
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.StageStyle

class ScopeClientPreloader: Preloader() {
    private lateinit var preloaderStage: Stage

    override fun start(stage: Stage) {
        preloaderStage = stage

        val splashImageLocation = ScopeClientPreloader::class.java.classLoader.getResourceAsStream("scope_splash.png")
        val splashImage = Image(splashImageLocation)
        val imageView = ImageView(splashImage)
        imageView.fitWidth = splashImage.width / 2.0
        imageView.fitHeight = splashImage.height / 2.0

        val iconLocation = ScopeClientPreloader::class.java.classLoader.getResourceAsStream("scope_icon.png")
        val icon = Image(iconLocation)
        stage.icons.add(icon)

        val pane = BorderPane(imageView)
        val scene = Scene(pane)

        val screenBounds = Screen.getPrimary().bounds

        stage.scene = scene
        stage.width = imageView.fitWidth - 1.0
        stage.height = imageView.fitHeight - 1.0
        stage.initStyle(StageStyle.UNDECORATED)
        stage.x = screenBounds.width / 2.0 - stage.width / 2.0
        stage.y = screenBounds.height / 2.0 - stage.height / 2.0
        stage.show()
    }

    override fun handleStateChangeNotification(info: StateChangeNotification) {
        if (info.type == StateChangeNotification.Type.BEFORE_START) {
            preloaderStage.close() //Close splash screen before starting
        }
    }
}