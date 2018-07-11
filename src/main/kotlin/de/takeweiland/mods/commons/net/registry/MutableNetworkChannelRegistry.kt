package de.takeweiland.mods.commons.net.registry

import de.takeweiland.mods.commons.net.base.AsyncNetworkChannel
import de.takeweiland.mods.commons.net.base.NetworkChannel
import de.takeweiland.mods.commons.net.base.asAsync

/**
 * @author Take Weiland
 */
interface MutableNetworkChannelRegistry : NetworkChannelRegistry {

    fun register(channel: AsyncNetworkChannel<*>)

}

fun MutableNetworkChannelRegistry.register(channel: NetworkChannel<*>) {
    register(channel.asAsync())
}