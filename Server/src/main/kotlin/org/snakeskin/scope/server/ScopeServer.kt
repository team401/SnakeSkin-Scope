package org.snakeskin.scope.server

import org.snakeskin.scope.protocol.ScopeProtocol
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
 * This class implements the "Runnable" interface to make it easy to update periodically with Java's built in
 * executor framework, threads, or any other periodic loop executor.
 *
 * Please note that this class automatically creates one thread per client connected to maintain the connection.
 */
abstract class ScopeServer(val timesource: IScopeTimeSource, val dataServerPort: Int = 4010, val headerServerPort: Int = 4011): Runnable {
    /**
     * Method to register your channels.  The provided registration "context" object
     * has methods inside to register different types of channels.  Store the returned objects from these methods
     * in variables inside your class, and then use the methods inside those objects to update values for each channel.
     */
    abstract fun registerChannels(reg: ScopeRegistrationContext)

    /**
     * Method to update your channels.  Use the channel objects you stored from the "registerChannels" function
     * to sequentially update the value on each channel.
     */
    abstract fun updateChannels()

    private val registrationContext = ScopeRegistrationContext()
    private lateinit var protocol: ScopeProtocol
    private lateinit var writeBuffer: ByteBuffer
    private val datagramChannel = DatagramChannel.open()
    private lateinit var headerServer: ScopeHeaderServer

    /**
     * Call this method before calling "run" for the first time.
     */
    fun init() {
        registerChannels(registrationContext) //Registers the channels
        protocol = ScopeProtocol(registrationContext.registeredChannels) //Create a protocol from registered channels
        writeBuffer = ByteBuffer.allocateDirect(protocol.sizeBytes) //Allocate a write buffer
        headerServer = ScopeHeaderServer(headerServerPort, dataServerPort, protocol, 10 * 1000)
        headerServer.start()
    }

    /**
     * Call this method periodically to send out new scope values to the client.  The more often this method is called,
     * the more data will be sent to the client.  For best results, it is recommended to call this function at a fixed rate
     * so that the data will be evenly spaced on the plot.
     */
    override fun run() {
        val now = timesource.getTimeNow() //Get the current time
        updateChannels() //Update all channel values //TODO maybe optimize so this directly writes to the buffer?
        protocol.populateBuffer(now, writeBuffer) //Fill the write buffer with the data
        writeBuffer.rewind()
        val clients = headerServer.getClients()
        println(clients)
        clients.forEach {
            datagramChannel.send(writeBuffer, it.dataSocketAddress) //Send the buffer to the client
        }
    }
}

fun main() {
    val ts = object : IScopeTimeSource {
        override fun getTimeNow(): Double {
            return System.nanoTime() * 1e-9
        }
    }

    val server = object : ScopeServer(ts) {
        lateinit var channel1: ScopeChannelNumeric
        lateinit var channel2: ScopeChannelNumeric

        override fun registerChannels(reg: ScopeRegistrationContext) {
            channel1 = reg.registerNumeric("Test Channel 1")
            channel2 = reg.registerNumeric("Test Channel 2")
        }

        override fun updateChannels() {
            channel1.update(Math.random())
            channel2.update(Math.random())
        }
    }

    server.init()

    while (true) {
        server.run()
        Thread.sleep(100)
    }
}