package de.takeweiland.mods.commons.net

import net.minecraft.network.NetworkManager

/**
 * @author Take Weiland
 */
interface NetworkSendable<R> {

    fun sendTo(network: NetworkManager): R

}