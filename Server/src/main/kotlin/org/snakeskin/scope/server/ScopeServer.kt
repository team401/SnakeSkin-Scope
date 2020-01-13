package org.snakeskin.scope.server

import org.snakeskin.scope.protocol.ScopeProtocol
import org.snakeskin.scope.protocol.channel.ScopeChannelBoolean
import org.snakeskin.scope.protocol.channel.ScopeChannelNumeric
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

/**
 * The main scope server class.  This class manages the data server and the "header" server, which is used to
 * establish and maintain connections with clients.  A port number is required for each one.
 *
 * The default data server port is 4010, and the default header server port is 4011
 *
 * Please note that this class automatically creates one thread per client connected to maintain the connection.
 */
abstract class ScopeServer(val dataServerPort: Int = 4010, val headerServerPort: Int = 4011) {
    protected val channels = ScopeRegistrationContext()

    /**
     * Method to update your channels.  Use the channel objects you stored from the "registerChannels" function
     * to sequentially update the value on each channel.
     */
    protected abstract fun updateChannels()

    private lateinit var protocol: ScopeProtocol
    private lateinit var writeBuffer: ByteBuffer
    private val datagramChannel = DatagramChannel.open()
    private lateinit var headerServer: ScopeHeaderServer

    /**
     * Call this method before calling "run" for the first time.
     */
    fun init() {
        protocol = ScopeProtocol(dataServerPort, channels.registeredChannels) //Create a protocol from registered channels
        writeBuffer = ByteBuffer.allocateDirect(protocol.sizeBytes) //Allocate a write buffer
        headerServer = ScopeHeaderServer(headerServerPort, dataServerPort, protocol)
        headerServer.start()
    }

    /**
     * Call this method periodically to send out new scope values to the client.  The more often this method is called,
     * the more data will be sent to the client.  For best results, it is recommended to call this function at a fixed rate
     * so that the data will be evenly spaced on the plot.
     *
     * @param now The current relative system timestamp in seconds
     */
    fun run(now: Double) {
        updateChannels() //Update all channel values //TODO maybe optimize so this directly writes to the buffer?
        protocol.populateBuffer(now, writeBuffer) //Fill the write buffer with the data
        val clients = headerServer.getClients()
        clients.forEach {
            writeBuffer.rewind()
            datagramChannel.send(writeBuffer, it.dataSocketAddress) //Send the buffer to the client
        }
    }
}