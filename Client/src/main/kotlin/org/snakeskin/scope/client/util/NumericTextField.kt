package org.snakeskin.scope.client.util

import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.control.TextField
import java.text.NumberFormat
import java.text.ParseException

class NumericTextField(initialValue: Double): TextField() {
    private val nf = NumberFormat.getInstance()
    private val numberProperty = SimpleDoubleProperty(initialValue)

    fun numberProperty() = numberProperty
    fun getNumber() = numberProperty.get()
    fun setNumber(value: Double) = numberProperty.set(value)

    init {
        initHandlers()
        text = nf.format(initialValue)
        setNumber(initialValue)
    }

    private fun initHandlers() {
        setOnAction {
            parseAndFormatInput()
        }

        focusedProperty().addListener {
                _, _, newValue ->
            if (!newValue) {
                parseAndFormatInput()
            }
        }

        numberProperty().addListener {
                _, _, newValue ->
            text = nf.format(newValue)
        }
    }

    private fun parseAndFormatInput() {
        try {
            val input = text
            if (input == null || input.isEmpty()) {
                return
            }
            val parsedNumber = nf.parse(input)
            setNumber(parsedNumber.toDouble())
            selectAll()
        } catch (e: ParseException) {
            text = nf.format(numberProperty.get())
        }
    }
}