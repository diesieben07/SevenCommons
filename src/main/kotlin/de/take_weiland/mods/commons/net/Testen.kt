@file:Mod.EventBusSubscriber(modid = "sctestmod")

package de.take_weiland.mods.commons.net

import de.take_weiland.mods.commons.net.packet.NamePacket
import de.take_weiland.mods.commons.net.packet.raw.PacketChannel
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent

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