package org.snakeskin.scope.client

import org.snakeskin.scope.protocol.ScopeProtocol
import org.snakeskin.scope.protocol.channel.ScopeChannelNumeric
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

fun main() {
    val socket = Socket("localhost", 4011)
    val dgam = DatagramChannel.open()
    dgam.bind(InetSocketAddress("localhost", 4010))
    val out = socket.getOutputStream()
    val inFromServer = socket.getInputStream().bufferedReader()
    val header = inFromServer.readLine()

    println(header)
    val protocol = ScopeProtocol.deserializeProtocol(header)
    val buffer = ByteBuffer.allocate(protocol.sizeBytes)

    Thread {
        while (true) {
            out.write(0)
            Thread.sleep(1000)
        }
    }.start() //Keep alive thread

    while (true) {
        buffer.rewind()
        dgam.receive(buffer)
        protocol.populateChannels(buffer)
        println((protocol.channels[0] as ScopeChannelNumeric).currentValue)
    }
}