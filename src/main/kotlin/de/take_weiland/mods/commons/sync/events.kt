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
internal fun serverTick(event: TickEvent.ServerTickEvent) {
    if (event.phase == TickEvent.Phase.END && changedProperties.isNotEmpty()) {
        changedProperties.forEach { (key, value) ->
            @Suppress("UNCHECKED_CAST")
            (value as ChangedPropertyList<Any>).send(key)
        }
        changedProperties.clear()
    }
}
