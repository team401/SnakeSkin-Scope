package org.snakeskin.scope.protocol

/**
 * Commands the client can send to the server over the header socket
 */
enum class ScopeProtocolCommands {
    Heartbeat,
    HeaderRequest,

    NULL;

    companion object {
        fun getCommand(idx: Int): ScopeProtocolCommands {
            return when (idx) {
                0 -> Heartbeat
                1 -> HeaderRequest

                else -> NULL
            }
        }
    }
}