package org.snakeskin.scope.server

import org.snakeskin.scope.protocol.ScopeProtocol
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList

class ScopeHeaderServer(port: Int, val dataPort: Int, protocol: ScopeProtocol, val readTimeoutMs: Int) {
    private val header = (protocol.serializeProtocol() + "\n").toByteArray() //Pre-generate the protocol header
    private val serverSocket = ServerSocket(port)

    private var serverThread: Thread? = null
    private val clients = CopyOnWriteArrayList<ServerClientTask>() //This makes iterators threadsafe, and we'll be iterating a lot more than writing

    fun getClients(): List<ServerClientTask> { //Downcast to List to disable mutability
        return clients
    }

    inner class ServerAcceptTask: Runnable {
        override fun run() {
            while (!Thread.interrupted()) {
                try {
                    val clientSocket = serverSocket.accept() //Accept a new connection
                    clientSocket.soTimeout = readTimeoutMs
                    val clientTask = ServerClientTask(clientSocket)
                    clients.forEach {
                        if (it.address == clientSocket.inetAddress) {
                            it.stop() //Remove duplicate connections (if they somehow exist)
                        }
                    }
                    clientTask.start() //Register and start the client task
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
        }
    }

    inner class ServerClientTask(clientSocket: Socket): Runnable {
        private val thread = Thread(this)
        val address = clientSocket.inetAddress
        val dataSocketAddress = InetSocketAddress(address, dataPort)

        private val input = clientSocket.getInputStream()
        private val output = clientSocket.getOutputStream()

        fun start() {
            clients.add(this)
            thread.start()
        }

        fun stop() {
            clients.remove(this) //Remove ourselves from the client list
            thread.interrupt() //Stop the thread
        }

        override fun run() {
            //Once we know we're connected, send the header to the client once
            output.write(header)
            while (!Thread.interrupted()) {
                try {
                    val read = input.read()
                    if (read == -1) stop() //Connection lost
                } catch (e: InterruptedException) {
                    stop()
                }
            }
        }

        override fun toString(): String {
            return address.hostName
        }
    }

    /**
     * Starts the header server
     */
    @Synchronized fun start() {
        stop() //Stop any existing server

        serverThread = Thread(ServerAcceptTask())
        serverThread?.start()
    }

    /**
     * Stops the header server
     */
    @Synchronized fun stop() {
        serverThread?.interrupt()
        clients.forEach { it.stop() }
    }
}