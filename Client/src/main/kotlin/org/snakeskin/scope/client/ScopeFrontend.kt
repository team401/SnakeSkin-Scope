package org.snakeskin.scope.client

import org.snakeskin.scope.client.buffer.*
import org.snakeskin.scope.client.plot.PlotDrawingContext
import org.snakeskin.scope.client.timebase.TimebaseDrawingContext
import org.snakeskin.scope.protocol.ScopeProtocol
import java.nio.ByteBuffer
import kotlin.math.min

/**
 * Global frontend for the scope.  Manages all the logic for the scope, and collects all data.  Also controls
 * connection managers.
 */
object ScopeFrontend {
    /**
     * How many points to store in the frontend buffers
     */
    private const val FRONTEND_BUFFER_SIZE = 100000

    //Protocols and buffers
    private var protocol = ScopeProtocol(0, listOf())
    var incomingBuffer = ByteBuffer.allocate(protocol.sizeBytes) //TODO make private!
    private var bufferPointer = 0 //Pointer used to synchronize all buffers
    val channelBuffers = arrayListOf<ChannelBuffer>()
    val timestampBuffer = TimestampBuffer(FRONTEND_BUFFER_SIZE)

    //Synchronization
    private val bufferPointerLock = Any()

    //Frontend parameters
    private var numTimebaseDivisions = 9
    private var secondsPerTimebaseDivision = 0.1
    private var timebaseEndIndex = Int.MAX_VALUE //MAX_VALUE means use roll graphing mode
    @Synchronized get
    @Synchronized set

    /**
     * Returns true if the scope is running, false otherwise.
     */
    fun isRunning(): Boolean {
        return timebaseEndIndex == Int.MAX_VALUE
    }

    /**
     * Accepts a new protocol into the frontend.  This should be called whenever a new protocol is received from
     * the server.  This method is responsible for updating the channels, etc.
     */
    fun acceptProtocol(newProtocol: ScopeProtocol) {
        DRAW_LOCK.lock()
        synchronized(bufferPointerLock) {
            protocol = newProtocol
            bufferPointer = 0
            incomingBuffer = ByteBuffer.allocate(newProtocol.sizeBytes)
            channelBuffers.clear()
            timestampBuffer.reset()
            newProtocol.channels.forEach {
                //Create channel buffers for each channel
                channelBuffers.add(ChannelBuffer.create(it, FRONTEND_BUFFER_SIZE))
            }
        }
        DRAW_LOCK.unlock()
    }

    /**
     * Accepts a new data point from the server.  It is assumed that the data has already been loaded into the
     * incoming buffer for processing.
     */
    fun acceptData() {
        val timestamp = protocol.populateChannels(incomingBuffer)
        timestampBuffer.addTimestamp(bufferPointer, timestamp)
        channelBuffers.forEach {
            it.update(bufferPointer) //Update each channel buffer
        }
        synchronized(bufferPointerLock) {
            bufferPointer++ //Increment the buffer pointer
            if (bufferPointer >= FRONTEND_BUFFER_SIZE) {
                bufferPointer = FRONTEND_BUFFER_SIZE - 1 //Do not allow the pointer to get bigger than the size
            }
        }
    }

    /**
     * Starts data acquisition of the scope
     */
    fun startAcquisition() {
        timebaseEndIndex = Int.MAX_VALUE //Start the scope
    }

    /**
     * Stops data acquisition of the scope
     */
    fun stopAcquisition() {
        synchronized(bufferPointerLock) {
            timebaseEndIndex = bufferPointer - 1
        }
    }

    /**
     * Copies parameters for drawing the timebase into the provided DrawingContext.  These parameters can then be used
     * to draw the timebase slider background on the plot
     */
    fun updateTimebase(ctx: TimebaseDrawingContext) {
        val latestIndex: Int
        synchronized(bufferPointerLock) {
            latestIndex = bufferPointer - 1 //Latest data index
        }

        ctx.lastDataIndex = latestIndex
        ctx.running = timebaseEndIndex == Int.MAX_VALUE
    }

    /**
     * Copies parameters for drawing into the provided DrawingContext.  These parameters can then be used to safely
     * concurrently access the incoming data buffers.
     *
     * This function must be called while in possession of the draw lock
     */
    fun updateDraw(ctx: PlotDrawingContext) {
        ctx.numTimebaseDivisions = numTimebaseDivisions
        ctx.timebaseSecondsPerDivision = secondsPerTimebaseDivision

        //Since we are never looking ahead in the incoming buffers, grabbing this value once under lock
        //is sufficient to synchronize access to the array, at the expense of potentially missing the latest point
        //on a draw cycle.
        val latestIndex: Int
        synchronized(bufferPointerLock) {
            latestIndex = bufferPointer - 1 //Latest data index
        }

        val lastIndex = if (timebaseEndIndex == Int.MAX_VALUE) {
            latestIndex
        } else {
            min(latestIndex, timebaseEndIndex)
        }
        if (lastIndex <= 0) return //There is no data

        //We are in roll graphing mode
        ctx.lastIndex = lastIndex //Last index is always the latest datapoint in this mode
        val latestTime = timestampBuffer.arr[lastIndex] //Get the latest timestamp
        val firstTime = latestTime - (secondsPerTimebaseDivision * (numTimebaseDivisions + 1))
        if (firstTime > 0.0) {
            //First time value is positive, so we will do a rolling graph from the right
            ctx.timebaseLast = latestTime //Last division will be the latest time
            ctx.timebaseFirst = firstTime //Last division will be the first time
            ctx.firstIndex = timestampBuffer.searchForStart(firstTime, lastIndex) //Search for first index
        } else {
            //First time value is 0 or negative, so we will be doing a fixed graph from the left
            ctx.timebaseLast = secondsPerTimebaseDivision * (numTimebaseDivisions + 1)
            ctx.timebaseFirst = 0.0 //We will be graphing from 0
            ctx.firstIndex = 0 //We will be graphing all data
        }
    }
}