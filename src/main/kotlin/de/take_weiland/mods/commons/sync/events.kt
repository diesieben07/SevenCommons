@file:Mod.EventBusSubscriber(modid = "sevencommons")

package de.take_weiland.mods.commons.sync

import de.take_weiland.mods.commons.util.isServer
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

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

}

fun main(args: Array<String>) {
    run {  }
}