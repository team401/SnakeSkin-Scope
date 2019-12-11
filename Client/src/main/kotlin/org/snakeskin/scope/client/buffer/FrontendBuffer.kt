package org.snakeskin.scope.client.buffer

import org.snakeskin.scope.protocol.ScopeProtocol

class FrontendBuffer(val protocol: ScopeProtocol, val size: Int) {
    private val timestampBuffer = DoubleArray(size)
    //private val channelBuffers =
}