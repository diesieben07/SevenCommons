package de.takeweiland.mods.commons.net

import de.takeweiland.mods.commons.net.base.NetworkSerializable
import de.takeweiland.mods.commons.net.codec.writeVarInt
import de.takeweiland.mods.commons.net.registry.getPlainPacketData
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.NetworkManager

/**
 * @author Take Weiland
 */
abstract class Packet : PacketBase(), NetworkSerializable, SimplePacket {

    abstract fun process(player: EntityPlayer)

    final override fun sendTo(network: NetworkManager) {
        val channel = network.channel()
        channel.writeAndFlush(this, channel.voidPromise())
    }

    final override val channel: String
        get() = getPlainPacketData(javaClass).channel

    final override fun getPacket(packetFactory: (String, ByteBuf) -> AnyMCPacket): AnyMCPacket {
        val data = getPlainPacketData(javaClass)
        val buf = Unpooled.buffer(expectedSize + 1)
        buf.writeVarInt(data.id)
        write(buf)
        return packetFactory(data.channel, buf)
    }
}