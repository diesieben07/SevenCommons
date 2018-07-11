package de.takeweiland.mods.commons.net.registry

import com.google.common.collect.ImmutableMap
import de.takeweiland.mods.commons.net.base.AsyncNetworkChannel

internal class FrozenNetworkChannelRegistryImpl(private val map: ImmutableMap<String, AsyncNetworkChannel<*>>) : NetworkChannelRegistry {

    override val channels: Collection<AsyncNetworkChannel<*>> get() = map.values

    override fun get(channel: String): AsyncNetworkChannel<*>? {
        return map[channel]
    }

    override fun freeze(): NetworkChannelRegistry {
        return this
    }
}