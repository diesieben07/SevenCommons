package de.take_weiland.mods.commons.sync

import de.take_weiland.mods.commons.net.packet.raw.CustomPayloadPacket
import de.take_weiland.mods.commons.net.writeVarInt
import de.take_weiland.mods.commons.util.clientPlayer
import de.take_weiland.mods.commons.util.clientThread
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.relauncher.Side

/**
 * @author diesieben07
 */
abstract class ChangedPropertyList<CONTAINER : Any> : ArrayList<Any?>(4), CustomPayloadPacket {

    final override val channel: String
        get() = containerType.channel

    abstract val containerType: SyncedContainerType<CONTAINER>

    protected abstract fun writeContainerData(buf: ByteBuf)
    protected abstract fun getContainer(player: EntityPlayer): CONTAINER?

    abstract fun send(obj: CONTAINER)

    override fun writePayload(buf: ByteBuf) {
        writeContainerData(buf)
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

    fun <P> addChange(property: SyncedProperty<P>, payload: P) {
        this += property
        this += payload
    }

    override fun receiveAsync(side: Side, player: EntityPlayer?) {
        clientThread {
            val container = getContainer(clientPlayer)
            if (container != null) {
                val accessor = PropertyAccessorCache.get(container.javaClass)

                var i = 0
                while (i < size) {
                    val remoteProperty = this[i] as SyncedProperty<*>

                    @Suppress("UNCHECKED_CAST")
                    val property = PropertyAccessorHelpers.accessProperty(accessor, container, remoteProperty.id) as SyncedProperty<Any?>?
                    property?.receivePayload(this[i+1])

                    i += 2
                }
            }
        }
    }
}