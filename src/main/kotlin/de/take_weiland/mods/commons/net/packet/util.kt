package de.take_weiland.mods.commons.net.packet

import de.take_weiland.mods.commons.net.readVarInt
import de.take_weiland.mods.commons.net.writeVarInt
import de.take_weiland.mods.commons.util.clientPlayer
import de.take_weiland.mods.commons.util.clientThread
import de.take_weiland.mods.commons.util.thread
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer

/**
 * @author diesieben07
 */
internal inline fun ByteBuf.writePacketId(id: Int) = writeVarInt(id)
internal inline fun ByteBuf.readPacketId() = readVarInt()

internal inline fun EntityPlayer?.runPacketOnThread(crossinline body: (EntityPlayer) -> Unit) {
    if (this == null) {
        clientThread { body(clientPlayer) }
    } else {
        thread.run { body(this) }
    }
}