package de.takeweiland.mods.commons

import de.takeweiland.mods.commons.fml.KOTLIN_LANGUAGE_ADAPTER
import de.takeweiland.mods.commons.net.register.networkChannel
import de.takeweiland.mods.commons.netbase.sendTo
import de.takeweiland.mods.commons.scheduler.MinecraftServerThread
import kotlinx.coroutines.experimental.launch
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.NetworkManager
import net.minecraft.util.EnumHand
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.logging.log4j.Logger
import java.util.*

internal lateinit var SC_LOG: Logger

/**
 * @author Take Weiland
 */
@Mod(modid = SevenCommons.ID, name = SevenCommons.NAME, version = SevenCommons.VERSION, modLanguageAdapter = KOTLIN_LANGUAGE_ADAPTER)
@Mod.EventBusSubscriber
object SevenCommons {

    @Mod.EventHandler
    internal fun preInit(event: FMLPreInitializationEvent) {
        SC_LOG = event.modLog

        networkChannel("SevenCommons") {
            packet(0, ::TestPacket)
            packet(1, ::TestPacketWithResponse, TestPacketWithResponse::Response)
        }

//        globalPayloadHandlerRegistry.register("SevenCommons", object : CustomPayloadHandler {
//            override fun handle(
//                channel: String, buf: ByteBuf, side: Side,
//                player: EntityPlayer?
//            ): Boolean {
//                println("$channel received ${buf.readInt()} on $side")
//                return true
//            }
//        })
    }

    @Mod.EventHandler
    internal fun init(event: FMLInitializationEvent) {
//        val asyncThing = async {
//            delay(15, TimeUnit.SECONDS)
//            println("other thread says hi!")
//            delay(4, TimeUnit.SECONDS)
//            println("other thread waited!")
//            "hello"
//        }
//        launch(MinecraftClientThread) {
//            println("client thread says hi!")
//            val awaited = asyncThing.await()
//            println("client thread got $awaited!")
//            delay(4, TimeUnit.SECONDS)
//            println("client thread waited!")
//        }
    }

    @SubscribeEvent
    @JvmStatic
    internal fun event(event: PlayerInteractEvent.RightClickBlock) {
        if (!event.entity.world.isRemote && event.hand == EnumHand.MAIN_HAND) {
            val number = Random().nextInt()
            println("server sending $number")
            val response = TestPacketWithResponse(number).sendTo(event.entityPlayer)
            launch(MinecraftServerThread) {
                println("response: " + response.await().number)
            }
//            object : CustomPayloadPacket {
//
//                override val expectedSize: Int
//                    get() = 4
//                override val channel: String
//                    get() = "SevenCommons"
//
//                override fun writePayload(buf: ByteBuf) {
//                    buf.writeInt(number)
//                }
//
//                override fun handle(player: EntityPlayer?, side: Side) {
//                    println("received $number directly on $side")
//                }
//            }.sendTo(event.entityPlayer)
        } else if (event.entity.world.isRemote && event.hand == EnumHand.MAIN_HAND) {
//            TestPacket(456).sendToServer()
        }
    }

    const val ID = "sevencommons"
    const val NAME = "SevenCommons"
    const val VERSION = "0.0.1-alpha"

    @SidedProxy
    internal lateinit var proxy: SCProxy

    internal interface SCProxy {

        val clientPlayer: EntityPlayer?
        val clientToServerNetworkManager: NetworkManager

    }

    @Suppress("unused")
    internal class ClientProxy : SCProxy {
        override val clientPlayer: EntityPlayer?
            get() = Minecraft.getMinecraft().player

        override val clientToServerNetworkManager: NetworkManager
            get() = Minecraft.getMinecraft().player.connection.networkManager
    }

    @Suppress("unused")
    internal class ServerProxy : SCProxy {
        override val clientPlayer: EntityPlayer?
            get() = throw IllegalStateException("Cannot get client player on the server")

        override val clientToServerNetworkManager: NetworkManager
            get() = throw IllegalStateException("Cannot get client to server NetworkManager on the server")
    }

}
