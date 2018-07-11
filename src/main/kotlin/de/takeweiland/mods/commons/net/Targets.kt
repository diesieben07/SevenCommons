package de.takeweiland.mods.commons.net

import de.takeweiland.mods.commons.SevenCommons
import de.takeweiland.mods.commons.util.allPlayers
import de.takeweiland.mods.commons.util.minecraftServer
import de.takeweiland.mods.commons.util.trackingPlayers
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP

/**
 * @author Take Weiland
 */
inline fun <M> NetworkSendable<*, M, NetworkMultiResultBuilder<*, M, *>>.sendMulti(body: NetworkMultiResultBuilder<*, M, *>.() -> Unit): M {
    val builder = newMultiResultBuilder()
    builder.body()
    return builder.finish()
}

private typealias ANS<R> = NetworkSendable<R, *, *>
private typealias ANSM<M> = NetworkSendable<*, M, NetworkMultiResultBuilder<*, M, *>>

fun <R> ANS<R>.sendToServer(): R = sendTo(SevenCommons.proxy.clientToServerNetworkManager)

@Suppress("NOTHING_TO_INLINE")
inline fun <R> ANS<R>.sendTo(player: EntityPlayer): R = sendTo(player as EntityPlayerMP)
@Suppress("NOTHING_TO_INLINE")
inline fun <R> ANS<R>.sendTo(player: EntityPlayerMP): R = sendTo(player.connection.networkManager)

fun <M> ANSM<M>.sendTo(vararg players: EntityPlayer): M = sendMulti {
    for (player in players) sendTo(player)
}

fun <M> ANSM<M>.sendTo(vararg players: EntityPlayerMP): M = sendMulti {
    for (player in players) sendTo(player)
}

@Suppress("NOTHING_TO_INLINE")
inline fun <M> ANSM<M>.sendTo(players: Iterable<EntityPlayerMP>): M = sendTo(players.iterator())

fun <M> ANSM<M>.sendTo(players: Iterator<EntityPlayerMP>): M = sendMulti {
    for (player in players) sendTo(player)
}

fun <M> ANSM<M>.sendToAll(): M = sendTo(minecraftServer.allPlayers)
fun <M> ANSM<M>.sendToTracking(entity: Entity): M = sendTo(entity.trackingPlayers)
fun <M> ANSM<M>.sendToSelfAndTracking(player: EntityPlayer): M = sendMulti {
    sendTo(player)
    sendToTracking(player)
}