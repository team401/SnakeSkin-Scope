package org.snakeskin.scope.server

import org.snakeskin.scope.protocol.channel.ScopeChannel
import org.snakeskin.scope.protocol.channel.ScopeChannelBoolean
import org.snakeskin.scope.protocol.channel.ScopeChannelNumeric
import org.snakeskin.scope.protocol.channel.ScopeChannelPose

class ScopeRegistrationContext {
    internal val registeredChannels = arrayListOf<ScopeChannel>()

    private fun checkName(name: String) {
        require(registeredChannels.all { it.name != name }) { "There is already a channel with the name '$name'." }
    }

    /**
     * Registers a numeric channel with the given name
     */
    fun registerNumeric(name: String): ScopeChannelNumeric {
        checkName(name)
        val channel = ScopeChannelNumeric(name)
        registeredChannels.add(channel)
        return channel
    }

    fun registerBoolean(name: String): ScopeChannelBoolean {
        checkName(name)
        val channel = ScopeChannelBoolean(name)
        registeredChannels.add(channel)
        return channel
    }

    fun registerPose(name: String): ScopeChannelPose {
        checkName(name)
        val channel = ScopeChannelPose(name)
        registeredChannels.add(channel)
        return channel
    }
}