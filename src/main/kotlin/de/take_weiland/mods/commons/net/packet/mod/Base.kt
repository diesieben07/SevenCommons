package de.take_weiland.mods.commons.net.packet.mod

import de.take_weiland.mods.commons.net.packet.data
import de.take_weiland.mods.commons.net.packet.raw.CustomPayloadPacket
import de.take_weiland.mods.commons.net.packet.writePacketId
import de.take_weiland.mods.commons.net.simple.SimplePacket
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.NetworkManager
import net.minecraftforge.fml.relauncher.Side

/**
 * @author diesieben07
 */
const val defaultExpectedPacketSize = 32

abstract class BaseModPacket internal constructor() {

    abstract fun write(buf: ByteBuf)

    open val expectedSize: Int get() = defaultExpectedPacketSize

}

abstract class ModPacketWithResponse<out R : Packet.Response> internal constructor(): BaseModPacket() {

    internal abstract fun receiveAfterReconstruction(responseId: Int, side: Side, player: EntityPlayer?)

}

abstract class ModPacketNoResponse internal constructor() : BaseModPacket(), CustomPayloadPacket {

    final override fun writePayload(buf: ByteBuf) {
        buf.writePacketId(data.packetId)
        write(buf)
    }

    final override val expectedPayloadSize: Int
        get() = expectedSize + 1

    final override val channel: String
        get() = data.channel

    internal abstract fun receiveAfterReconstruction(side: Side, player: EntityPlayer?)

    final override fun sendTo(manager: NetworkManager) = super.sendTo(manager)
    final override fun newMultiResultBuilder(): SimplePacket = super.newMultiResultBuilder()
    final override fun finish() = super.finish()
}