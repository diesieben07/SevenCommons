package de.takeweiland.mods.commons.net

import de.takeweiland.mods.commons.net.codec.writeVarInt
import de.takeweiland.mods.commons.net.registry.getResponsePacketData
import de.takeweiland.mods.commons.netbase.BasicCustomPayloadPacket
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.relauncher.Side

/**
 * @author Take Weiland
 */
abstract class ResponsePacket : PacketBase(), BasicCustomPayloadPacket {

    @JvmField
    internal var responseId = -1

    override fun <R> serialize(factory: (channel: String, buf: ByteBuf) -> R): R {
        val data = getResponsePacketData(javaClass)
        val buf = Unpooled.buffer(expectedSize + 2)
        buf.writeVarInt(data.id)
        buf.writeByte(RESPONSE_MARKER_BIT or responseId)
        write(buf)
        return factory(data.channel, buf)
    }

    override fun handle(player: EntityPlayer?, side: Side) {
        throw IllegalStateException()
    }
}