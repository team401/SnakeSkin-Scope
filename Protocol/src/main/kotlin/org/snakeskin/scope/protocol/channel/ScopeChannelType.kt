package org.snakeskin.scope.protocol.channel

/**
 * Enum representing available channel types.
 * @param dataSizeBytes The size that this type of channel occupies, in bytes
 */
enum class ScopeChannelType(val dataSizeBytes: Int) {
    Numeric(8), //Numeric is represented by 1 double
    Boolean(1), //8 booleans could be packed into a single byte, but for now we'll make it easier
    Pose(8 * 3) //Pose is represented by 3 doubles
}