package de.takeweiland.mods.commons.net

import de.takeweiland.mods.commons.SevenCommons
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP

/**
 * @author Take Weiland
 */
fun <R> NetworkSendable<R>.sendToServer(): R = sendTo(SevenCommons.proxy.clientToServerNetworkManager)

fun <R> NetworkSendable<R>.sendTo(player: EntityPlayer) = sendTo(player as EntityPlayerMP)
fun <R> NetworkSendable<R>.sendTo(player: EntityPlayerMP) = sendTo(player.connection.networkManager)