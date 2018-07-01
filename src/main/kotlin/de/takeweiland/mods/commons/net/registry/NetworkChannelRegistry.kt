package de.takeweiland.mods.commons.net.registry

import de.takeweiland.mods.commons.net.base.NetworkChannel

/**
 * @author Take Weiland
 */
interface NetworkChannelRegistry {

    val channels: Collection<NetworkChannel<*>>

    operator fun get(channel: String): NetworkChannel<*>?

    fun freeze(): NetworkChannelRegistry

}