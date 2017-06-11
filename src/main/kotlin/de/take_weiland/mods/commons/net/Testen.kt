@file:Mod.EventBusSubscriber(modid = "sctestmod")

package de.take_weiland.mods.commons.net

import de.take_weiland.mods.commons.net.packet.NamePacket
import de.take_weiland.mods.commons.net.packet.raw.PacketChannel
import de.take_weiland.mods.commons.util.serverInstance
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * @author diesieben07
 */
@Mod(modid = "sctestmod")
class TestMod {

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        val channel = PacketChannel("SevenCommons") {
            +NamePacket.Reader
        }
    }

}


@SubscribeEvent
fun rightClick(event: PlayerInteractEvent.RightClickBlock) {
    val server = serverInstance
}