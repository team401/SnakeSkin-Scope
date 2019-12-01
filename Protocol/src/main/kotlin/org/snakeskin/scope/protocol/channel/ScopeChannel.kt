package org.snakeskin.scope.protocol.channel

sealed class ScopeChannel(val name: String) {
    abstract val type: ScopeChannelType
}

/**
 * A numeric scope channel, which represents a numeric variable changing with respect to time.
 */
class ScopeChannelNumeric(name: String): ScopeChannel(name) {
    override val type = ScopeChannelType.Numeric

    var currentValue = 0.0
    private set

    /**
     * Updates the value of this numeric channel with new data.
     * @param value The new value
     */
    fun update(value: Double) {
        currentValue = value
    }

    /**
     * Convenience function for integer values.
     * @param value The new value
     */
    fun update(value: Int) {
        update(value.toDouble())
    }
}

/**
 * A boolean scope channel, which represents a boolean (logic) variable changing with respect to time.
 */
class ScopeChannelBoolean(name: String): ScopeChannel(name) {
    override val type = ScopeChannelType.Boolean

    var currentValue: Boolean = false
    private set

    /**
     * Current value of the boolean channel as a byte
     * This is a convenience function used in the protocol serialization
     */
    fun currentValueAsByte(): Byte {
        return if (currentValue) 1.toByte() else 0.toByte()
    }

    /**
     * Updates the value of this boolean channel with new data.
     * @param value The new value
     */
    fun update(value: Boolean) {
        currentValue = value
    }

    /**
     * Convenience function for protocol decoding
     */
    fun updateFromByte(byteValue: Byte) {
        update(byteValue == 1.toByte())
    }
}

/**
 * A pose scope channel, which represents a position or transformation in 2D space changing with respect to time.
 */
class ScopeChannelPose(name: String): ScopeChannel(name) {
    override val type = ScopeChannelType.Pose

    var currentX: Double = 0.0
    private set

    var currentY: Double = 0.0
    private set

    var currentTheta: Double = 0.0
    private set

    /**
     * Updates the value of this pose channel with new data.
     * The coordinate system used is x positive is forward, and y positive is to the left.
     * @param x The x position, in inches
     * @param y The y position, in inches
     * @param theta The angle, in radians
     */
    fun update(x: Double, y: Double, theta: Double) {
        currentX = x
        currentY = y
        currentTheta = theta
    }
}