package org.snakeskin.scope.client.buffer

import org.snakeskin.scope.protocol.channel.ScopeChannel
import org.snakeskin.scope.protocol.channel.ScopeChannelBoolean
import org.snakeskin.scope.protocol.channel.ScopeChannelNumeric
import org.snakeskin.scope.protocol.channel.ScopeChannelPose

sealed class ChannelBuffer {
    abstract val size: Int
    abstract fun reset()
    abstract fun update(ptr: Int)

    companion object {
        /**
         * Creates a new channel buffer for the given channel
         */
        fun create(channel: ScopeChannel, size: Int): ChannelBuffer {
            return when (channel) {
                is ScopeChannelNumeric -> NumericChannelBuffer(size, channel)
                is ScopeChannelBoolean -> BooleanChannelBuffer(size, channel)
                is ScopeChannelPose -> PoseChannelBuffer(size, channel)
            }
        }
    }
}

class NumericChannelBuffer(override val size: Int, val channel: ScopeChannelNumeric): ChannelBuffer() {
    val arr = DoubleArray(size)

    override fun reset() {
        arr.fill(0.0)
    }

    override fun update(ptr: Int) {
        arr[ptr] = channel.currentValue
    }
}

class BooleanChannelBuffer(override val size: Int, val channel: ScopeChannelBoolean): ChannelBuffer() {
    val arr = BooleanArray(size)

    override fun reset() {
        arr.fill(false)
    }

    override fun update(ptr: Int) {
        arr[ptr] = channel.currentValue
    }
}

class PoseChannelBuffer(override val size: Int, val channel: ScopeChannelPose): ChannelBuffer() {
    val xArr = DoubleArray(size)
    val yArr = DoubleArray(size)
    val thetaArr = DoubleArray(size)

    override fun reset() {
        xArr.fill(0.0)
        yArr.fill(0.0)
        thetaArr.fill(0.0)
    }

    override fun update(ptr: Int) {
        xArr[ptr] = channel.currentX
        yArr[ptr] = channel.currentY
        thetaArr[ptr] = channel.currentTheta
    }
}