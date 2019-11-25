package org.snakeskin.scope.client

import javafx.animation.AnimationTimer
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Application
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.VPos
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.control.Label
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import javafx.stage.Stage
import javafx.util.Duration
import java.text.DecimalFormat


class ScopeClient: Application() {
    override fun start(stage: Stage) {
        val javaVersion = System.getProperty("java.version")
        val javafxVersion = System.getProperty("javafx.version")
        val c1 = Canvas(640.0, 480.0)
        val c2 = Canvas(640.0, 480.0)
        val stack = StackPane(c1, c2)
        stack.background = Background(BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY))
        val scene = Scene(stack, 640.0, 480.0)
        val bg = c1.graphicsContext2D
        val fg = c2.graphicsContext2D

        val padding = 30.0

        bg.stroke = Color.WHITE
        bg.strokeRect(padding, padding, c1.width - 2 * padding, c1.height - 2 * padding)
        bg.setLineDashes(2.0, 4.0)
        val pixelsPerLine = (c1.width - padding) / 20.0
        for (i in padding.toInt() .. (c1.width - 2 * padding).toInt() step pixelsPerLine.toInt()) {
            bg.strokeLine(i.toDouble(), padding, i.toDouble(), c1.height - padding)
        }


        var lastTime = 0L

        val fmt = DecimalFormat("###.##")

        val timer = object : AnimationTimer() {
            override fun handle(now: Long) {
                fg.clearRect(0.0, 0.0, c2.width, c2.height)
                val fps = 1000.0 / ((now - lastTime) * 1e-6)
                val truncated = fmt.format(fps).toString()
                fg.fillText(truncated, 0.0, 0.0)
                lastTime = now
            }
        }
        timer.start()

        stage.scene = scene
        stage.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(ScopeClient::class.java)
        }
    }
}