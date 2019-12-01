package org.snakeskin.scope.server

import org.snakeskin.scope.protocol.ScopeProtocol

/**
 * The main scope server class.
 *
 * This class implements the "Runnable" interface to make it easy to update periodically with Java's built in
 * executor framework, threads, or any other periodic loop executor.
 *
 * Please note that this class automatically creates one thread per client connected to maintain the connection.
 */
abstract class ScopeServer(val timesource: IScopeTimeSource): Runnable {
    /**
     * Method to register your channels.  The provided registration "context" object
     * has methods inside to register different types of channels.  Store the returned objects from these methods
     * in variables inside your class, and then use the methods inside those objects to update values for each channel.
     */
    abstract fun registerChannels(registration: ScopeRegistrationContext)

    /**
     * Method to update your channels.  Use the channel objects you stored from the "registerChannels" function
     * to sequentially update the value on each channel.
     */
    abstract fun updateChannels()

    private val registrationContext = ScopeRegistrationContext()
    private lateinit var protocol: ScopeProtocol

    /**
     * Call this method before calling "run" for the first time.
     */
    fun init() {
        registerChannels(registrationContext) //Registers the channels
        protocol = ScopeProtocol(registrationContext.registeredChannels) //Create a protocol from registered channels
    }

    /**
     * Call this method periodically to send out new scope values to the client.  The more often this method is called,
     * the more data will be sent to the client.  For best results, it is recommended to call this function at a fixed rate
     * so that the data will be evenly spaced on the plot.
     */
    override fun run() {
        val now = timesource.getTimeNow() //Get the current time
        updateChannels() //Update all channel values
        //TODO pack and send data
    }
}