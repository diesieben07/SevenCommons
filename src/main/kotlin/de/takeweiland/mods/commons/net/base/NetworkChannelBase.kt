package de.takeweiland.mods.commons.net.base

import de.takeweiland.mods.commons.net.obtainForNetworkOnMainThread
import de.takeweiland.mods.commons.scheduler.mainThread
import io.netty.buffer.ByteBuf
import kotlinx.coroutines.experimental.launch
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.relauncher.Side

/**
 * @author Take Weiland
 */
typealias NetworkChannel<M> = NetworkChannelBase<M, EntityPlayer>

typealias AsyncNetworkChannel<M> = NetworkChannelBase<M, EntityPlayer?>

interface NetworkChannelBase<M : NetworkSerializable, P : EntityPlayer?> {

    val name: String

    fun receive(buf: ByteBuf, side: Side, player: P)

    fun receive(message: M, side: Side, player: P)

}

private class NetworkChannelSyncAdapter<M : NetworkSerializable>(private val delegate: NetworkChannel<M>) : AsyncNetworkChannel<M> {

    override val name: String
        get() = delegate.name

    override fun receive(buf: ByteBuf, side: Side, player: EntityPlayer?) {
        launch(side.mainThread) {
            delegate.receive(buf, side, player.obtainForNetworkOnMainThread())
        }
    }

    override fun receive(message: M, side: Side, player: EntityPlayer?) {
        launch(side.mainThread) {
            delegate.receive(message, side, player.obtainForNetworkOnMainThread())
        }
    }
}

internal fun <M : NetworkSerializable> NetworkChannel<M>.asAsync(): AsyncNetworkChannel<M> {
    return NetworkChannelSyncAdapter(this)
}