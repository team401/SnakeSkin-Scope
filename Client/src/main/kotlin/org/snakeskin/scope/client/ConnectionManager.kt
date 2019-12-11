package org.snakeskin.scope.client

import org.snakeskin.scope.protocol.ScopeProtocol
import org.snakeskin.scope.protocol.ScopeProtocolCommands
import java.net.Socket
import java.util.concurrent.Executors

/**
 * Manages the connection with the server.  Automatically reconnects if disconnected.
 */
object ConnectionManager {
    private var socket: Socket? = null
    private var protocol: ScopeProtocol? = null

    private val executor = Executors.newSingleThreadScheduledExecutor()
    //private val onConnectTasks

    /**
     * Closes any existing connections and resets the connection manager to its initial state.
     */
    @Synchronized fun reset() {
        socket?.close()
        socket = null
        protocol = null
    }

    /**
     * Attempts to make a new connection at the given server address and header port number
     *
     * @return True if the connection was established successfully, false otherwise.
     */
    @Synchronized fun newConnection(address: String, headerPort: Int): Boolean {
        reset()
        try {
            socket = Socket(address, headerPort)
            if (!downloadProtocol()) return false //If we couldn't get the protocol, return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return false
    }

    /**
     * Requests the header from the server.  If it is received, the "protocol" field is populated
     * Otherwise, no states change.
     *
     * @return True if the protocol was populated, false otherwise
     */
    private fun downloadProtocol(): Boolean {
        if (socket == null) return false //Return immediately if there is no socket
        val socket = socket!! //Bypass nullability (safe because all access to this is synchronized)
        val input = socket.getInputStream().bufferedReader() //Get the input stream as a buffered reader
        val output = socket.getOutputStream() //Get the output stream
        val oldTimeout = socket.soTimeout //Store old socket timeout
        socket.soTimeout = ScopeProtocol.HEADER_REQ_TIMEOUT_MS //Set socket timeout to the header timeout

        output.write(ScopeProtocolCommands.HeaderRequest.ordinal) //Request the header from the server

        return try {
            val header = input.readLine()
            protocol = ScopeProtocol.deserializeProtocol(header) //Store the protocol
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            socket.soTimeout = oldTimeout //Reset socket timeout
        }
    }
}