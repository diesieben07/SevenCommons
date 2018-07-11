package de.takeweiland.mods.commons.net.registry

import de.takeweiland.mods.commons.net.base.AsyncNetworkChannel

/**
 * @author Take Weiland
 */
interface NetworkChannelRegistry {

    val channels: Collection<AsyncNetworkChannel<*>>

    operator fun get(channel: String): AsyncNetworkChannel<*>?

    fun freeze(): NetworkChannelRegistry

}