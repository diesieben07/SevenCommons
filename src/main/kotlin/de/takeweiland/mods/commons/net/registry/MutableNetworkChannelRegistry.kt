package de.takeweiland.mods.commons.net.registry

import de.takeweiland.mods.commons.net.base.NetworkChannel

/**
 * @author Take Weiland
 */
interface MutableNetworkChannelRegistry : NetworkChannelRegistry {

    fun register(channel: NetworkChannel<*>)

}