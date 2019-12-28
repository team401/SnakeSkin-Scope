package org.snakeskin.scope.client.plot

import javafx.geometry.VPos
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import java.text.DecimalFormat

/**
 * Utilities for drawing on a canvas
 */
object DrawingUtils {
    //Fix 3 decimal places, allow up to 12 decimal places
    val axisNumberFormatter = DecimalFormat("#0.000") //Number formatter for formatting all axis numbers

    fun drawDot(ctx: GraphicsContext, x: Double, y: Double, radius: Double) {
        ctx.fillOval(x - radius, y - radius, radius * 2.0, radius * 2.0)
    }

    fun drawDotLineVertical(ctx: GraphicsContext, xPos: Double, yStart: Double, yHeight: Double, numDots: Double, dotRadius: Double) {
        val dotSpacing = yHeight / numDots

        if (dotSpacing == 0.0) return //This happens on very small heights, refuse to draw this

        var currentPos = yStart
        while (currentPos <= yStart + yHeight) {
            drawDot(ctx, xPos, currentPos, dotRadius)
            currentPos += dotSpacing
        }
    }

    fun drawDotLineHorizontal(ctx: GraphicsContext, yPos: Double, xStart: Double, xWidth: Double, numDots: Double, dotRadius: Double) {
        val dotSpacing = xWidth / numDots

        if (dotSpacing == 0.0) return //This happens on very small widths, refuse to draw this

        var currentPos = xStart
        while (currentPos <= xStart + xWidth) {
            drawDot(ctx, currentPos, yPos, dotRadius)
            currentPos += dotSpacing
        }
    }

    /**
     * Draws the plot outline and timebase for a plot
     */
    fun drawPlotAndTimebase(ctx: GraphicsContext, width: Double, height: Double, xOffset: Double, numTimebaseDivisions: Int, numDots: Double, dotRadius: Double) {
        ctx.save()

        //Draw outline
        ctx.stroke = Color.WHITE
        //Offsets y by one to ensure that the top line is always visible, meaning we need to subtract 2 from height
        //Offsets width by one to update only ending coordinate, as starting x always draws properly
        ctx.strokeRect(xOffset, 1.0, width - xOffset - 1.0, height - 2.0)

        //Draw timebase
        ctx.fill = Color.WHITE
        val widthPerDivision = (width - xOffset) / (numTimebaseDivisions + 1)
        for (i in 1..numTimebaseDivisions) {
            val timebaseX = xOffset + (i * widthPerDivision)
            if (timebaseX < xOffset) continue //Do not draw lines that fall before the start (can happen if window is too small somehow)
            drawDotLineVertical(ctx, timebaseX, 0.0, height, numDots, dotRadius)
        }

        ctx.restore()
    }

    fun drawVerticalNumeric(ctx: GraphicsContext, width: Double, height: Double, xOffset: Double, numVerticalDivisions: Int, numDots: Double, dotRadius: Double, axisMin: Double, axisMax: Double) {
        ctx.save()

        val heightPerDivision = height / (numVerticalDivisions + 1)

        val valueRange = axisMax - axisMin
        val valuesPerDivision = valueRange / (numVerticalDivisions + 1)

        val zeroY = ((axisMin + valueRange) / valueRange) * height

        if (axisMin != 0.0 && axisMax != 0.0) {
            ctx.stroke = Color.DARKGREY //Draw a zero line if the min or max isn't zero
            ctx.strokeLine(xOffset, zeroY, width, zeroY)
        }

        ctx.fill = Color.GREY
        for (i in 1..(numVerticalDivisions + 1)) {
            val divisionY = i * heightPerDivision
            //Draw division
            drawDotLineHorizontal(ctx, divisionY, xOffset, width - xOffset, numDots, dotRadius)
        }

        ctx.fill = Color.GREY
        ctx.textAlign = TextAlignment.RIGHT //Draw text from the right and centered vertically
        ctx.textBaseline = VPos.CENTER //This makes it very simple to pick coordinates to put the text
        ctx.font = Font.font(12.0)
        for (i in 1..numVerticalDivisions) {
            //Draw text for divisions
            val divisionY = i * heightPerDivision
            ctx.fillText(axisNumberFormatter.format(axisMax - (i * valuesPerDivision)), xOffset - 5.0, divisionY)
        }

        //Draw text for top and bottom
        ctx.fill = Color.WHITE
        ctx.font = Font.font(12.0)
        ctx.textBaseline = VPos.TOP
        ctx.fillText(axisNumberFormatter.format(axisMax), xOffset - 5.0, 0.0)
        ctx.textBaseline = VPos.BOTTOM
        ctx.fillText(axisNumberFormatter.format(axisMin), xOffset - 5.0, height)

        ctx.font = Font.font(14.0)
        ctx.textAlign = TextAlignment.CENTER
        ctx.textBaseline = VPos.CENTER
        ctx.translate(xOffset - 75.0, height / 2.0)
        ctx.rotate(-90.0)
        ctx.fillText("Bus Voltage (Volts)", 0.0, 0.0)

        ctx.restore() //Restore to remove rotation
    }
}