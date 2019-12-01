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
import javafx.scene.canvas.GraphicsContext
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

fun drawDotLineVertical(ctx: GraphicsContext, xPos: Double, yStart: Double, yHeight: Double, numDots: Double, dotRadius: Double) {
    val dotSpacing = yHeight / numDots

    var currentPos = yStart
    while (currentPos <= yStart + yHeight) {
        ctx.fillOval(xPos - dotRadius, currentPos - dotRadius, dotRadius * 2.0, dotRadius * 2.0)
        currentPos += dotSpacing
    }
}

fun drawDotLineHorizontal(ctx: GraphicsContext, yPos: Double, xStart: Double, xWidth: Double, numDots: Double, dotRadius: Double) {
    val dotSpacing = xWidth / numDots

    var currentPos = xStart
    while (currentPos <= xStart + xWidth) {
        ctx.fillOval(currentPos - dotRadius, yPos - dotRadius, dotRadius * 2.0, dotRadius * 2.0)
        currentPos += dotSpacing
    }
}

class ScopeClient: Application() {
    override fun start(stage: Stage) {
        val windowWidth = 640.0
        val windowHeight = 480.0
        val c1 = Canvas(windowWidth, windowHeight)
        val c2 = Canvas(windowWidth, windowHeight)
        val stack = StackPane(c1, c2)
        stack.background = Background(BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY))
        val scene = Scene(stack, windowWidth, windowHeight)
        val bg = c1.graphicsContext2D
        val fg = c2.graphicsContext2D

        val padding = 30.0

        val width = c1.width - 2 * padding
        val height = c1.height - 2 * padding
        val lineSpacing = width / 10.0 //20 divisions


        bg.stroke = Color.WHITE
        bg.strokeRect(padding, padding, width, height)

        bg.textAlign = TextAlignment.CENTER
        bg.textBaseline = VPos.TOP
        bg.fill = Color.WHITE
        bg.font = Font.font(12.0)
        bg.stroke = Color.GREY
        bg.fill = Color.GREY

        var currentLine = padding + lineSpacing

        while (currentLine < padding + width) {
            //bg.strokeLine(currentLine, padding, currentLine, height + padding)
            drawDotLineVertical(bg, currentLine, padding, height, 30.0, 0.5)
            bg.fillText("0.001", currentLine, height + padding)
            currentLine += lineSpacing
        }

        val vLineSpacing = (height / 2.0) / 5.0 //20 divisions

        val centerLine = padding + (height / 2.0)
        //bg.strokeLine(padding, centerLine, width + padding, centerLine)
        drawDotLineHorizontal(bg, centerLine, padding, width, 60.0, 1.0)

        var currentVLine = centerLine + vLineSpacing

        while (currentVLine < padding + height) {
           // bg.strokeLine(padding, currentVLine, width + padding, currentVLine)
            drawDotLineHorizontal(bg, currentVLine, padding, width, 60.0, 0.5)
            currentVLine += vLineSpacing
        }

        currentVLine = centerLine - vLineSpacing

        while (currentVLine > padding) {
            //bg.strokeLine(padding, currentVLine, width + padding, currentVLine)
            drawDotLineHorizontal(bg, currentVLine, padding, width, 60.0, 0.5)
            currentVLine -= vLineSpacing
        }



        var lastTime = 0L

        val fmt = DecimalFormat("###.##")

        fg.textAlign = TextAlignment.LEFT
        fg.textBaseline = VPos.TOP
        fg.fill = Color.YELLOW
        fg.font = Font.font(24.0)

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