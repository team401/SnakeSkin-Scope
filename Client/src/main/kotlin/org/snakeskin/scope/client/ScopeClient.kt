package org.snakeskin.scope.client

import javafx.application.Application
import javafx.beans.InvalidationListener
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.Stage
import org.snakeskin.scope.client.plot.NumericPlot
import org.snakeskin.scope.client.plot.PlotManager

fun drawDot(ctx: GraphicsContext, x: Double, y: Double, radius: Double) {
    ctx.fillOval(x - radius, y - radius, radius * 2.0, radius * 2.0)
}

fun drawGraphPoint(ctx: GraphicsContext, padding: Double, xMin: Double, yMin: Double, xMax: Double, yMax: Double, width: Double, height: Double, xCoord: Double, yCoord: Double, radius: Double) {
    //Convert coordinates into percentage of distance into each axis
    val xPercent = xCoord / (xMax - xMin)
    val yPercent = 1.0 - (yCoord / (yMax - yMin) + .5)

    //Get pixel locations for each coordinate
    val xPixels = padding + width * xPercent
    val yPixels = padding + height * yPercent

    //Plot the point
    drawDot(ctx, xPixels, yPixels, radius)
}

fun drawGraphLine(ctx: GraphicsContext, padding: Double, xMin: Double, yMin: Double, xMax: Double, yMax: Double, width: Double, height: Double, xCoordStart: Double, yCoordStart: Double, xCoordEnd: Double, yCoordEnd: Double) {
    //Convert coordinates into percentage of distance into each axis
    val xStartPercent = xCoordStart / (xMax - xMin)
    val yStartPercent = 1.0 - (yCoordStart / (yMax - yMin) + .5)

    val xEndPercent = xCoordEnd / (xMax - xMin)
    val yEndPercent = 1.0 - (yCoordEnd / (yMax - yMin) + .5)


    //Get pixel locations for each coordinate
    val xStartPixels = padding + width * xStartPercent
    val yStartPixels = padding + height * yStartPercent

    val xEndPixels = padding + width * xEndPercent
    val yEndPixels = padding + height * yEndPercent

    //Plot the point
    ctx.strokeLine(xStartPixels, yStartPixels, xEndPixels, yEndPixels)
}

fun drawDotLineVertical(ctx: GraphicsContext, xPos: Double, yStart: Double, yHeight: Double, numDots: Double, dotRadius: Double) {
    val dotSpacing = yHeight / numDots

    var currentPos = yStart
    while (currentPos <= yStart + yHeight) {
        drawDot(ctx, xPos, currentPos, dotRadius)
        currentPos += dotSpacing
    }
}

fun drawDotLineHorizontal(ctx: GraphicsContext, yPos: Double, xStart: Double, xWidth: Double, numDots: Double, dotRadius: Double) {
    val dotSpacing = xWidth / numDots

    var currentPos = xStart
    while (currentPos <= xStart + xWidth) {
        drawDot(ctx, currentPos, yPos, dotRadius)
        currentPos += dotSpacing
    }
}

class ScopeClient: Application() {
    override fun start(stage: Stage) {
        //PlotManager.root.setMinSize(Pane.USE_PREF_SIZE, Pane.USE_PREF_SIZE)

        val rootAnchor = AnchorPane()
        val vbox = VBox()
        val hbox = HBox()
        vbox.background = Background(BackgroundFill(Color.YELLOW, CornerRadii.EMPTY, Insets.EMPTY))

        AnchorPane.setTopAnchor(vbox, 0.0)
        AnchorPane.setBottomAnchor(vbox, 0.0)
        AnchorPane.setLeftAnchor(vbox, 0.0)
        AnchorPane.setRightAnchor(vbox, 0.0)

        rootAnchor.children.add(vbox)
        vbox.children.add(hbox)

        PlotManager.root.setMinSize(640.0, 480.0)
        PlotManager.root.background = Background(BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY))

        hbox.children.add(PlotManager.root)

        PlotManager.start()
        PlotManager.addPlot(NumericPlot())
        val scene = Scene(rootAnchor)
        stage.scene = scene

        val listener = InvalidationListener {
            val width = stage.width
            val height = stage.height
            if (!width.isNaN() && !height.isNaN()) {
                //Resize the root
                val targetWidth = width - 200.0
                val targetHeight = height - 100.0

                PlotManager.resizeRoot(targetWidth, targetHeight)
            }
        }

        stage.widthProperty().addListener(listener)
        stage.heightProperty().addListener(listener)

        stage.minWidth = 640.0 + 200.0
        stage.minHeight = 480.0 + 100.0

        stage.width = stage.minWidth
        stage.height = stage.minHeight

        stage.show()

        /*

        for (i in 0 until 1) {
            PlotManager.addPlot(NumericPlot())
        }

        val scene = Scene(PlotManager.root)

        stage.scene = scene
        stage.widthProperty().addListener(InvalidationListener {
            if (!stage.height.isNaN() && !stage.width.isNaN())
            PlotManager.resizeRoot(stage.width, stage.height)
        })

        stage.heightProperty().addListener(InvalidationListener {
            if (!stage.height.isNaN() && !stage.width.isNaN())
                PlotManager.resizeRoot(stage.width, stage.height)
        })



        stage.show()

         */

        /*
        val windowWidth = 640.0
        val windowHeight = 480.0
        val c1 = Canvas(windowWidth, windowHeight)
        val c2 = Canvas(windowWidth, windowHeight)
        val button = Button("asdf")
        val button2 = Button("jkl;")

        StackPane.setAlignment(button, Pos.TOP_LEFT)
        StackPane.setAlignment(button2, Pos.TOP_LEFT)
        button.translateX = 100.0
        button2.translateX = 110.0

        c1.translateX = 120.0

        val stack = StackPane(c1, c2, button, button2)
        //stack.background = Background(BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY))
        val scene = Scene(stack, windowWidth, windowHeight)
        val bg = c1.graphicsContext2D
        val fg = c2.graphicsContext2D

        val padding = 30.0

        val width = c1.width - 2 * padding
        val height = c1.height - 2 * padding
        val lineSpacing = width / 10.0 //20 divisions

        bg.fill = Color.BLACK
        bg.fillRect(0.0, 0.0, windowWidth, windowHeight)

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
        fg.stroke =  Color.CYAN

        val timer = object : AnimationTimer() {
            override fun handle(now: Long) {
                fg.clearRect(0.0, 0.0, c2.width, c2.height)
                val fps = 1000.0 / ((now - lastTime) * 1e-6)
                val truncated = fmt.format(fps).toString()
                fg.fillText(truncated, 0.0, 0.0)

                var tNow = 0.0
                var tLast = 0.0
                var yLast = Math.random()
                drawGraphPoint(fg, padding, 0.0, -1.0, 1.0, 1.0, width, height, tNow, yLast, 2.0)
                tNow = 0.001

                while (tNow <= .9) {
                    //Draw next point
                    val y = Math.sin(tNow * 20.0) / 5.0 + Math.random() / 10.0 - Math.random() / 10.0
                    //drawGraphPoint(fg, padding, 0.0, -1.0, 1.0, 1.0, width, height, tNow, y, 2.0)
                    //Draw line to last point
                    drawGraphLine(fg, padding, 0.0, -1.0, 1.0, 1.0, width, height, tLast, yLast, tNow, y)
                    yLast = y
                    tLast = tNow
                    tNow += 0.001
                }

                lastTime = now
            }
        }
        timer.start()

        val executor = Executors.newSingleThreadScheduledExecutor()

        executor.schedule({
            Platform.runLater {
                try {
                    val drawBuffer =
                        BufferedImage(windowWidth.toInt(), windowHeight.toInt(), BufferedImage.TYPE_INT_ARGB)

                    val bgRender = SwingFXUtils.fromFXImage(c1.snapshot(SnapshotParameters(), null), null)
                    val fgRender = SwingFXUtils.fromFXImage(c2.snapshot(object : SnapshotParameters() {
                        override fun getFill() = Color.TRANSPARENT
                    }, null), null)

                    val g = drawBuffer.graphics

                    g.drawImage(bgRender, 0, 0, null)
                    g.drawImage(fgRender, 0, 0, null)

                    ImageIO.getWriterFormatNames().forEach {println(it)}
                    ImageIO.write(drawBuffer, "png", File("C:/Users/cameronearle/Desktop/test3.png"))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }, 5L, TimeUnit.SECONDS)

        stage.scene = scene
        stage.show()

         */
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(ScopeClient::class.java)
        }
    }
}