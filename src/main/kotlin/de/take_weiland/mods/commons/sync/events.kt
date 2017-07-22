@file:Mod.EventBusSubscriber(modid = "sevencommons")
package de.take_weiland.mods.commons.sync

import net.minecraftforge.fml.common.Mod

/**
 * @author diesieben07
 */
//@SubscribeEvent
//fun serverTick(event: TickEvent.ServerTickEvent) {
//    changedProperties.forEach { (key, value) ->
//        handleChangedProperties(key, value)
//    }
//
//    changedProperties.clear()
//}
//
//fun <T : Any> handleChangedProperties(obj: T, changed: List<ChangedProperty<*>>) {
//    val containerType: SyncedContainerType<T, *> = findContainerType(obj)
//
//
//
//    containerType.sendPacket(obj, object : CustomPayloadPacket {
//
//        override val channel: String
//            get() = syncChannel
//
//        override fun writePayload(buf: ByteBuf) {
//
//            buf.writeList(changed) {
//                it.write(this)
//            }
//        }
//
//        override fun receiveAsync(side: Side, player: EntityPlayer?) {
//            if (side.isClient) {
//                containerType.serialize(obj)
//            }
//        }
//    })
//}
//
//internal fun <T> ChangedProperty.write(buf: ByteBuf) {
//    buf.writeVarInt(property.id)
//    property.writeData(buf, valueGeneric)
//}