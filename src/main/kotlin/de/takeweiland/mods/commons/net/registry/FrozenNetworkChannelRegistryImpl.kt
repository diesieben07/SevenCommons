package de.takeweiland.mods.commons.net.registry

import com.google.common.collect.ImmutableMap
import de.takeweiland.mods.commons.net.base.NetworkChannel

internal class FrozenNetworkChannelRegistryImpl(private val map: ImmutableMap<String, NetworkChannel<*>>) : NetworkChannelRegistry {

    override val channels: Collection<NetworkChannel<*>> get() = map.values

    override fun get(channel: String): NetworkChannel<*>? {
        return map[channel]
    }

    override fun freeze(): NetworkChannelRegistry {
        return this
    }
}