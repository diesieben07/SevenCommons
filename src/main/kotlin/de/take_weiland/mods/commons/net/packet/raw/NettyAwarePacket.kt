package de.take_weiland.mods.commons.net.packet.raw

import de.take_weiland.mods.commons.net.packet.defaultExpectedPacketSize
import de.take_weiland.mods.commons.net.simple.SimplePacket
import io.netty.buffer.ByteBuf
import net.minecraft.network.NetworkManager

/**
 * @author diesieben07
 */
interface NettyAwarePacket : SimplePacket {

    val channel: String

    val bufferPreSize: Int
        get() = defaultExpectedPacketSize

    fun writeForRemote(buf: ByteBuf)

    fun getLocalReceive(): NettyAsyncReceive

    override fun sendTo(manager: NetworkManager) {
        if (manager.isLocalChannel) {
            manager.channel().writeAndFlush(getLocalReceive())
        } else {
            manager.channel().writeAndFlush(this)
        }
    }
}
