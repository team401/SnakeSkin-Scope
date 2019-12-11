package org.snakeskin.scope.client.buffer

import org.snakeskin.scope.protocol.channel.ScopeChannelBoolean
import org.snakeskin.scope.protocol.channel.ScopeChannelNumeric
import org.snakeskin.scope.protocol.channel.ScopeChannelPose

sealed class ChannelBuffer(val size: Int) {
    abstract fun reset()
    abstract fun update()
}

class NumericChannelBuffer(val channel: ScopeChannelNumeric, size: Int): ChannelBuffer(size) {
    private val buffer = DoubleArray(size)
    private var ptrIdx = 0

    override fun reset() {
        for (i in 0 until size) {
            buffer[i] = 0.0
        }
        ptrIdx = 0
    }

    @Synchronized override fun update() {
        val value = channel.currentValue
        if (ptrIdx < size) {
            buffer[ptrIdx] = value //Place the value at the pointer
            ptrIdx++ //Increment the pointer
        } else {
            System.arraycopy(buffer, 1, buffer, 0, size - 1) //Left shift the array
            buffer[size - 1] = value //Place the value at the last index
        }
    }

    @Synchronized fun copyOut(traceBuffer: DoubleArray) {
        val srcIdx = ptrIdx - traceBuffer.size
        if (srcIdx < 0) {
            //Source buffer is not up to trace buffer capacity yet, left fill only available data
            System.arraycopy(buffer, 0, traceBuffer, 0, ptrIdx)
        } else {
            //Source buffer is at or over trace buffer capacity, left fill the size of the trace buffer
            System.arraycopy(buffer, srcIdx, traceBuffer, 0, traceBuffer.size)
        }
    }
}

class BooleanChannelBuffer(val channel: ScopeChannelBoolean, size: Int): ChannelBuffer(size) {
    private val buffer = BooleanArray(size)
    private var ptrIdx = 0

    override fun reset() {
        for (i in 0 until size) {
            buffer[i] = false
            ptrIdx = 0
        }
    }

    override fun update() {
        val value = channel.currentValue
        if (ptrIdx < size) {
            buffer[ptrIdx] = value //Place the value at the pointer
            ptrIdx++ //Increment the pointer
        } else {
            System.arraycopy(buffer, 1, buffer, 0, size - 1) //Left shift the array
            buffer[size - 1] = value //Place the value at the last index
        }
    }
}

class PoseChannelBuffer(val channel: ScopeChannelPose, size: Int): ChannelBuffer(size) {
    private val xBuffer = DoubleArray(size)
    private val yBuffer = DoubleArray(size)
    private val thetaBuffer = DoubleArray(size)
    private var ptrIdx = 0

    override fun reset() {
        for (i in 0 until size) {
            xBuffer[i] = 0.0
            yBuffer[i] = 0.0
            thetaBuffer[i] = 0.0
        }
        ptrIdx = 0
    }

    override fun update() {
        val xValue = channel.currentX
        val yValue = channel.currentY
        val thetaValue = channel.currentTheta

        if (ptrIdx < size) {
            xBuffer[ptrIdx] = xValue
            yBuffer[ptrIdx] = yValue
            thetaBuffer[ptrIdx] = thetaValue
            ptrIdx++
        } else {
            System.arraycopy(xBuffer, 1, xBuffer, 0, size - 1)
            System.arraycopy(yBuffer, 1, yBuffer, 0, size - 1)
            System.arraycopy(thetaBuffer, 1, thetaBuffer, 0, size - 1)
            xBuffer[size - 1] = xValue
            yBuffer[size - 1] = yValue
            thetaBuffer[size - 1] = thetaValue
        }
    }
}

fun main() {
    val channel = ScopeChannelNumeric("Test")
    val buffer = NumericChannelBuffer(channel, 100)
    val traceBuffer = DoubleArray(10) { Double.NaN }
    for (i in 0 until 1050) {
        channel.update(i.toDouble())
        buffer.update()
    }
    buffer.copyOut(traceBuffer)
    traceBuffer.forEach {
        print("$it ")
    }
}