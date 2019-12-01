package org.snakeskin.scope.protocol

import org.snakeskin.scope.protocol.channel.*
import java.nio.ByteBuffer

/**
 * A scope protocol.  Handles serialization and deserialization for scope channel data, as well as serialization and
 * deserialization of itself for sending over the network.
 *
 * Channels are referenced by index, not by name.  The order of the input channel list matters.
 */
class ScopeProtocol(val channels: List<ScopeChannel>) {
    companion object {
        /**
         * Deserializes a header into a protocol object
         */
        fun deserializeProtocol(header: String): ScopeProtocol {
            val channelHeaders = header.split(';')
            val channelList = arrayListOf<ScopeChannel>()
            channelHeaders.forEach  {
                val split = it.split(':')
                val name = split[0]
                val typeOrdinal = split[1].toInt()
                val channel = when (ScopeChannelType.values()[typeOrdinal]) {
                    ScopeChannelType.Numeric -> ScopeChannelNumeric(name)
                    ScopeChannelType.Boolean -> ScopeChannelBoolean(name)
                    ScopeChannelType.Pose -> ScopeChannelPose(name)
                }
                channelList.add(channel)
            }
            return ScopeProtocol(channelList)
        }
    }

    /**
     * Serializes a header for this protocol object.  This header can then be sent to a client,
     * where it can be reconstructed into a protocol object to keep the ends in sync.
     */
    fun serializeProtocol(): String {
        return channels.joinToString(";") { "${it.name}:${it.type.ordinal}" } //Simple serialization of the channel data
    }

    /**
     * Byte buffer indices for each channel value.
     */
    private val offsets = IntArray(channels.size)

    init {
        //Calculate offsets
        var currentOffset = 8 //Start at 8, since we include the timestamp as the first 8 bytes
        channels.forEachIndexed { index, scopeChannel ->
            offsets[index] = currentOffset
            currentOffset += scopeChannel.type.dataSizeBytes //Increment offset by data size
        }
    }

    /**
     * The size of this protocol in bytes.
     */
    val sizeBytes = channels.sumBy { it.type.dataSizeBytes } + 8 //8 bytes for the timestamp

    /**
     * Fills a byte buffer with the current value of each channel.
     * This function should be called on the server side to populate the transmission buffer
     * with new data from the scope.
     */
    fun populateBuffer(timestamp: Double, buffer: ByteBuffer) {
        check (buffer.capacity() >= sizeBytes) //Ensure there's enough room in the buffer for our data
        buffer.putDouble(0, timestamp) //Write the timestamp to the first slot in the buffer
        channels.forEachIndexed { index, scopeChannel ->
            val offset = offsets[index] //Grab the byte offset for this channel
            when (scopeChannel) {
                is ScopeChannelNumeric -> buffer.putDouble(offset, scopeChannel.currentValue)
                is ScopeChannelBoolean -> buffer.put(offset, scopeChannel.currentValueAsByte())
                is ScopeChannelPose -> {
                    //Pose requires 3 double write operations
                    buffer.putDouble(offset, scopeChannel.currentX)
                    buffer.putDouble(offset + 8, scopeChannel.currentY) //Offset plus 8 bytes
                    buffer.putDouble(offset + 16, scopeChannel.currentTheta) //Offset plus 16 bytes
                }
            }
        }
    }

    /**
     * Updates the scope channels with the values from the byte buffer.
     * This function should be called on the client side to populate the channel objects
     * with new data from the buffer.
     *
     * @return The timestamp of this data
     */
    fun populateChannels(buffer: ByteBuffer): Double {
        val timestamp = buffer.getDouble(0)
        channels.forEachIndexed { index, scopeChannel ->
            val offset = offsets[index]
            when (scopeChannel) {
                is ScopeChannelNumeric -> scopeChannel.update(buffer.getDouble(offset))
                is ScopeChannelBoolean -> scopeChannel.updateFromByte(buffer.get(offset))
                is ScopeChannelPose -> {
                    //Pose requires 3 double read operations
                    scopeChannel.update(
                        buffer.getDouble(offset),
                        buffer.getDouble(offset + 8), //Offset plus 8 bytes
                        buffer.getDouble(offset + 16) //Offset plus 16 bytes
                    )
                }
            }
        }
        return timestamp
    }
}