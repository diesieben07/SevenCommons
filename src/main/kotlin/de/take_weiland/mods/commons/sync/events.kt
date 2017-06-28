@file:Mod.EventBusSubscriber(modid = "sevencommons")

package de.take_weiland.mods.commons.sync

import de.take_weiland.mods.commons.net.packet.raw.CustomPayloadPacket
import de.take_weiland.mods.commons.net.writeList
import de.take_weiland.mods.commons.net.writeVarInt
import de.take_weiland.mods.commons.util.isServer
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.relauncher.Side

/**
 * @author diesieben07
 */
@SubscribeEvent
fun worldTick(event: TickEvent.WorldTickEvent) {
    if (event.world.isServer) {
        changedProperties.forEach { (key, value) ->
            handleChangedProperties(key, value)
        }
    }
}

fun <T : Any> handleChangedProperties(obj: T, changed: List<ChangedProperty<*>>) {
    val containerType: SyncedContainerType<T, *> = findContainerType(obj)
        
    containerType.sendPacket(obj, object : CustomPayloadPacket.Sendable {

        override val channel: String
            get() = syncChannel

        override fun writePayload(buf: ByteBuf) {
            buf.writeList(changed) {
                it.write(this)
            }
        }

        private fun <T> ChangedProperty<T>.write(buf: ByteBuf) {
            buf.writeVarInt(property.id)
            property.writeData(buf, valueGeneric)
        }

        override fun receiveAsync(side: Side, player: EntityPlayer?) {
            if (side.isClient) {
                containerType.serialize(obj)
            }
        }
    })
}
