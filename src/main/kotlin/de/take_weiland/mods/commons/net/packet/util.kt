package de.take_weiland.mods.commons.net.packet

import de.take_weiland.mods.commons.net.readVarInt
import de.take_weiland.mods.commons.net.writeVarInt
import io.netty.buffer.ByteBuf

/**
 * @author diesieben07
 */
internal inline fun ByteBuf.writePacketId(id: Int) = writeVarInt(id)
internal inline fun ByteBuf.readPacketId() = readVarInt()

