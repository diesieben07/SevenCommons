package de.take_weiland.mods.commons.sync

import de.take_weiland.mods.commons.net.packet.raw.CustomPayloadPacket
import de.take_weiland.mods.commons.net.writeVarInt
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.relauncher.Side

/**
 * @author diesieben07
 */
abstract class ChangedPropertyList<CONTAINER> : ArrayList<Any?>(4), CustomPayloadPacket {

    final override val channel: String
        get() = syncChannel

    abstract fun grabData(container: CONTAINER)

    protected abstract fun writeData(buf: ByteBuf)

    override fun writePayload(buf: ByteBuf) {
        writeData(buf)
        buf.writeVarInt(size shr 1)
        var i = 0
        while (i < size) {
            @Suppress("UNCHECKED_CAST")
            val property = this[i] as SyncedProperty<Any?>

            buf.writeVarInt(property.id)
            property.writePayload(buf, this[i + 1])

            i += 2
        }
    }

    override fun receiveAsync(side: Side, player: EntityPlayer?) {
//        propertyById
    }
}