package de.takeweiland.mods.commons.netbase

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet

/**
 * @author Take Weiland
 */
internal class PayloadHandlerRegistryImpl : PayloadHandlerRegistry.Mutable {

    private companion object {
        val reservedChannels: Set<String> = ImmutableSet.of("REGISTER", "UNREGISTER", "FML", "FORGE")
    }

    private val map = HashMap<String, CustomPayloadHandler>()

    override fun getHandler(channel: String): CustomPayloadHandler? {
        return map[channel]
    }

    private fun String.isReservedChannel(): Boolean {
        return this in reservedChannels || startsWith("MC|")
    }

    override fun register(channel: String, handler: CustomPayloadHandler) {
        require(channel.length <= 20) { "Channel $channel is too long" }
        require(!channel.isReservedChannel()) { "Channel $channel is reserved" }
        if (map.putIfAbsent(channel, handler) != null) {
            throw IllegalArgumentException("Channel $channel is already registered.")
        }
    }

    fun freeze(): Map<String, CustomPayloadHandler> = ImmutableMap.copyOf(map)

}