package de.takeweiland.mods.commons.net.base

import de.takeweiland.mods.commons.net.AnyMCPacket
import io.netty.buffer.ByteBuf
import net.minecraft.network.Packet as MCPacket

/**
 * @author Take Weiland
 */
interface NetworkSerializable {

    val channel: String

    fun getPacket(packetFactory: (String, ByteBuf) -> AnyMCPacket): AnyMCPacket

}