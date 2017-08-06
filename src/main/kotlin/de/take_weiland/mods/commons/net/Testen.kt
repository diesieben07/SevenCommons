@file:Mod.EventBusSubscriber(modid = "sctestmod")

package de.take_weiland.mods.commons.net

import de.take_weiland.mods.commons.KotlinLanguageAdapter
import de.take_weiland.mods.commons.net.packet.mod.Packet
import de.take_weiland.mods.commons.net.packet.raw.PacketChannel
import de.take_weiland.mods.commons.net.simple.sendToServer
import de.take_weiland.mods.commons.util.Scheduler
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumHand
import net.minecraft.util.text.TextComponentString
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import java.util.function.Consumer

/**
 * @author diesieben07
 */
@Mod.EventBusSubscriber
@Mod(modid = "sctestmod", modLanguageAdapter = KotlinLanguageAdapter.name)
object TestMod {

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        val channel = PacketChannel.Simple("SCTEST") {
            packet(::TestPacket, ::TestResponse)
        }
    }

    @JvmStatic
    @SubscribeEvent
    fun rightClick(event: PlayerInteractEvent.RightClickEmpty) {
        if (event.side.isClient && event.hand == EnumHand.MAIN_HAND) {
            val stage = TestPacket("hello").sendToServer()
            stage.thenAcceptAsync(Consumer<TestResponse> {
                event.entityPlayer.sendMessage(TextComponentString("response: ${it.text}"))
            }, Scheduler.client)
        }
    }


}

class TestResponse : Packet.Response {

    val text: String

    constructor(text: String) {
        this.text = text
    }

    constructor(buf: ByteBuf) {
        text = buf.readString()
    }

    override fun write(buf: ByteBuf) {
        buf.writeString(text)
    }

}

class TestPacket : Packet.WithResponse<TestResponse> {

    val text: String

    constructor(text: String) {
        this.text = text
    }

    constructor(buf: ByteBuf) {
        text = buf.readString()
    }

    override fun write(buf: ByteBuf) {
        buf.writeString(text)
    }

    override fun receive(side: Side, player: EntityPlayer): TestResponse {
        return TestResponse(text.toUpperCase())
    }

}