package de.takeweiland.mods.commons

import de.takeweiland.mods.commons.fml.KOTLIN_LANGUAGE_ADAPTER
import de.takeweiland.mods.commons.net.TestPacket
import de.takeweiland.mods.commons.net.register.networkChannel
import de.takeweiland.mods.commons.net.registry.MutableNetworkChannelRegistryImpl
import de.takeweiland.mods.commons.net.registry.NetworkChannelRegistry
import de.takeweiland.mods.commons.net.sendTo
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.NetworkManager
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent

/**
 * @author Take Weiland
 */
@Mod(modid = SevenCommons.ID, name = SevenCommons.NAME, version = SevenCommons.VERSION, modLanguageAdapter = KOTLIN_LANGUAGE_ADAPTER)
@Mod.EventBusSubscriber
object SevenCommons {

    @Mod.EventHandler
    internal fun preInit(event: FMLPreInitializationEvent) {
        networkChannel("SevenCommons") {
            0 % ::TestPacket
        }
    }

    internal var networkChannels: NetworkChannelRegistry = MutableNetworkChannelRegistryImpl()

    @SubscribeEvent
    @JvmStatic
    internal fun playerLogin(event: PlayerEvent.PlayerLoggedInEvent) {
        TestPacket(123).sendTo(event.player)
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
    internal class SeverProxy : SCProxy {
        override val clientPlayer: EntityPlayer?
            get() = throw IllegalStateException("Cannot get client player on the server")

        override val clientToServerNetworkManager: NetworkManager
            get() = throw IllegalStateException("Cannot get client to server NetworkManager on the server")
    }

}
