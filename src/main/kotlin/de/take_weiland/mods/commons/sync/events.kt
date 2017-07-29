@file:Mod.EventBusSubscriber(modid = SevenCommons.MOD_ID)
package de.take_weiland.mods.commons.sync

import de.take_weiland.mods.commons.internal.SevenCommons
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

/**
 * @author diesieben07
 */
@SubscribeEvent
fun serverTick(event: TickEvent.ServerTickEvent) {
    if (changedProperties.isNotEmpty()) {
        changedProperties.forEach { (key, value) ->
            @Suppress("UNCHECKED_CAST")
            handleChangedProperties(key, value as ChangedPropertyList<Any>)
        }

        changedProperties.clear()
    }
}

fun <T : Any> handleChangedProperties(obj: T, changed: ChangedPropertyList<T>) {
    changed.containerType.sendPacket(obj, changed)
}