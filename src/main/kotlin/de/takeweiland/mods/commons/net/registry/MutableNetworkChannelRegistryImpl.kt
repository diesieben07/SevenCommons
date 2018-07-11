package de.takeweiland.mods.commons.net.registry

import com.google.common.collect.ImmutableMap
import de.takeweiland.mods.commons.net.base.AsyncNetworkChannel
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.relauncher.Side
import java.util.*

internal class MutableNetworkChannelRegistryImpl : MutableNetworkChannelRegistry {

    private val map = HashMap<String, AsyncNetworkChannel<*>>()

    override val channels: Collection<AsyncNetworkChannel<*>> = Collections.unmodifiableCollection(map.values)

    override fun get(channel: String): AsyncNetworkChannel<*>? = map[channel]

    override fun register(channel: AsyncNetworkChannel<*>) {
        if (channel.name in map) {
            throw IllegalArgumentException("A network channel with name ${channel.name} is already registered.")
        }
        for (side in Side.values()) {
            if (NetworkRegistry.INSTANCE.hasChannel(channel.name, side)) {
                throw IllegalArgumentException("A network channel with name ${channel.name} is already registered to FML")
            }
        }
        map[channel.name] = channel
    }

    override fun freeze(): NetworkChannelRegistry {
        return FrozenNetworkChannelRegistryImpl(ImmutableMap.copyOf(map))
    }
}