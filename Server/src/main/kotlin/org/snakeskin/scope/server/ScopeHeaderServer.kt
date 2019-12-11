package org.snakeskin.scope.server

import org.snakeskin.scope.protocol.ScopeProtocol
import org.snakeskin.scope.protocol.ScopeProtocolCommands
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.CopyOnWriteArrayList

class ScopeHeaderServer(port: Int, val dataPort: Int, protocol: ScopeProtocol) {
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
                    clientSocket.soTimeout = ScopeProtocol.HEARTBEAT_TIMEOUT_MS
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

    inner class ServerClientTask(val clientSocket: Socket): Runnable {
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
            if (!thread.isInterrupted) {
                thread.interrupt() //Interrupt the thread (if it's not already)
            }
            //Close the socket
            clientSocket.close()
        }

        override fun run() {
            while (!Thread.interrupted()) {
                try {
                    val read = input.read()
                    if (read == -1) break //If we read a -1, the connection is lost, so drop the client
                    val cmd = ScopeProtocolCommands.getCommand(read)

                    if (cmd == ScopeProtocolCommands.HeaderRequest) {
                        output.write(header) //Write the header if the client requested it
                    }
                } catch (e: InterruptedException) {
                    break
                } catch (e: Exception) {
                    e.printStackTrace()
                    break
                }
            }
            stop()
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