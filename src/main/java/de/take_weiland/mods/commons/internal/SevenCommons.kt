package de.take_weiland.mods.commons.internal

import com.google.common.util.concurrent.ThreadFactoryBuilder
import de.take_weiland.mods.commons.KotlinLanguageAdapter
import de.take_weiland.mods.commons.client.Rendering
import de.take_weiland.mods.commons.crash.Crashing
import de.take_weiland.mods.commons.internal.client.ClientProxy
import de.take_weiland.mods.commons.internal.client.worldview.EmptyEntityRenderer
import de.take_weiland.mods.commons.internal.client.worldview.ViewEntity
import de.take_weiland.mods.commons.internal.exclude.ClassInfoSuperCache
import de.take_weiland.mods.commons.net.packet.raw.PacketChannel
import de.take_weiland.mods.commons.proxy.sidedProxy
import de.take_weiland.mods.commons.sync.*
import de.take_weiland.mods.commons.util.Logging
import de.take_weiland.mods.commons.worldview.ClientChunks
import net.minecraft.block.Block
import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.server.MinecraftServer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.client.registry.RenderingRegistry
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.discovery.ASMDataTable
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.ForgeRegistries
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.apache.logging.log4j.Logger
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit


@Mod(modid = SevenCommons.MOD_ID, name = "SevenCommons", version = SevenCommons.VERSION, modLanguageAdapter = KotlinLanguageAdapter.name)
@Mod.EventBusSubscriber
object SevenCommons {

    val log: Logger = Logging.getLogger("SevenCommons")

    const val VERSION = "1.0"
    const val MOD_ID = "sevencommons"

    var clientMainThreadID: Long = 0

    lateinit var commonScheduler: ScheduledExecutorService

    val proxy = sidedProxy(::ClientProxy, ::ServerProxy)

    @SideOnly(Side.CLIENT)
    @Mod.EventHandler
    fun clientPreInit(event: FMLPreInitializationEvent) {
        clientMainThreadID = Thread.currentThread().id
        RenderingRegistry.registerEntityRenderingHandler(ViewEntity::class.java, ::EmptyEntityRenderer)
        universalPreInit(event)
    }

    @JvmStatic
    @SubscribeEvent
    fun registerBlocks(event: RegistryEvent.Register<Block>) {
        event.registry.register(TestBlock().setRegistryName("testblock"))
        GameRegistry.registerTileEntity(Test::class.java, "sevencommons:test_tile")
    }

    @JvmStatic
    @SubscribeEvent
    fun registerItems(event: RegistryEvent.Register<Item>) {
        val block = ForgeRegistries.BLOCKS.getValue(ResourceLocation(MOD_ID, "testblock"))!!
        event.registry.register(ItemBlock(block).setRegistryName(block.registryName))
    }

    @SideOnly(Side.SERVER)
    @Mod.EventHandler
    fun serverPreInit(event: FMLPreInitializationEvent) {
        universalPreInit(event)
    }

    private fun universalPreInit(event: FMLPreInitializationEvent) {
        asmData = event.asmData

        config = Configuration(event.suggestedConfigurationFile)
        config.load()

        syncConfig(true)

        BuilderLeakDetect.init()

        MinecraftForge.EVENT_BUS.register(ForgeEventHandler())

        //        Network.newSimpleChannel("SevenCommons")
        //                .register(0, PacketContainerButton::new, PacketContainerButton::handle)
        //                .register(1, PacketInventoryName::new, PacketInventoryName::handle)
        //                .register(3, PacketRequestWorldInfo::new, PacketWorldInfo::new, PacketRequestWorldInfo::handle)
        //                .build();

        ClassInfoSuperCache.preInit()

        proxy.preInit(event)

        //        MinecraftForge.EVENT_BUS.register();

        if (config.hasChanged()) {
            config.save()
        }

        with(PacketChannel) {
            register(TileEntitySyncedType)
            register(EntitySyncedType)
            register(ContainerSyncedType)
        }
    }

    @Mod.EventHandler
    fun onServerStart(event: FMLServerStartingEvent) {
        event.registerServerCommand(object : CommandBase() {
            override fun getName(): String {
                return "loadchunk"
            }

            override fun getUsage(sender: ICommandSender): String {
                return "loadchunk <dim> <x>, <z>, <radius> [\"un\"]"
            }

            @Throws(CommandException::class)
            override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) {
                if (args.size != 4 && args.size != 5) {
                    throw CommandException("Invalid number of args")
                }

                val dim = CommandBase.parseInt(args[0])
                val x = CommandBase.parseInt(args[1])
                val z = CommandBase.parseInt(args[2])
                val r = CommandBase.parseInt(args[3])
                val un = args.size == 5 && args[4].equals("un", ignoreCase = true)

                val player = CommandBase.getCommandSenderAsPlayer(sender)
                for (x0 in x - r..x + r) {
                    for (z0 in z - r..z + r) {
                        if (un) {
                            ClientChunks.unloadChunk(player, dim, x0, z0)
                        } else {
                            ClientChunks.loadChunk(player, dim, x0, z0)
                        }
                    }
                }
            }
        })

        event.registerServerCommand(object : CommandBase() {
            override fun getName(): String {
                return "blockupdate"
            }

            override fun getUsage(sender: ICommandSender): String {
                return "blockupdate <x>, <y>, <z>"
            }

            @Throws(CommandException::class)
            override fun execute(server: MinecraftServer, sender: ICommandSender, args: Array<String>) {
                if (args.size != 3) {
                    throw CommandException("Invalid number of args")
                }

                val x = CommandBase.parseInt(args[0])
                val y = CommandBase.parseInt(args[1])
                val z = CommandBase.parseInt(args[2])

                //                getCommandSenderAsPlayer(sender).worldObj.notifyBlockUpdate(x, y, z);
            }
        })

    }


    lateinit var asmData: ASMDataTable

    lateinit var config: Configuration

    @JvmStatic fun main(args: Array<String>) {
        Rendering.drawColoredQuad(0, 0, 100, 100, 0xFF00FF, 128, -2f)
    }

    fun scLogger(channel: String): Logger {
        return Logging.getLogger("SC|" + channel)
    }

    fun syncConfig(initialStartup: Boolean) {
        val usernameCacheSize = config.getInt("usernameCacheSize", Configuration.CATEGORY_GENERAL, 500, 0, Integer.MAX_VALUE,
                "How many UUID->Username mappings should be kept in the cache, set to 0 to never cache (this is not recommended!)")
        UsernameCache.initCache(usernameCacheSize)

        val minCorePoolSize = config.get(Configuration.CATEGORY_GENERAL, "commonPoolMinSize", -1,
                "How many Threads to keep alive in the core async-pool even when idle, -1 for default (usually number of cores on the machine).",
                -1, Integer.MAX_VALUE)
        minCorePoolSize.setRequiresMcRestart(true)

        if (initialStartup) {
            startCorePool(minCorePoolSize.int)
        }
    }

    private fun startCorePool(size: Int) {
        val corePoolSize = if (size < 0) Runtime.getRuntime().availableProcessors() else size

        val threadFactory = ThreadFactoryBuilder()
                .setNameFormat("SevenCommonsPool %s")
                .setDaemon(true)
                .setUncaughtExceptionHandler(Crashing.mainThreadExceptionHandler())
                .build()

        commonScheduler = ScheduledThreadPoolExecutor(corePoolSize, threadFactory)
        Runtime.getRuntime().addShutdownHook(object : Thread() {

            override fun run() {
                commonScheduler.shutdown()
                if (!commonScheduler.isTerminated) {
                    log.info("Waiting max. 20 seconds for common thread pool to shut down")
                    try {
                        commonScheduler.awaitTermination(20, TimeUnit.SECONDS)
                        log.info("Common thread pool gracefully shut down")
                    } catch (x: Throwable) {
                        log.error("Exception occurred awaiting common thread pool shut down", x)
                    }

                }
            }
        })
    }

}// TODO
//        meta.name = "SevenCommons";
//        meta.modId = MOD_ID;
//        meta.authorList = ImmutableList.of("diesieben07");
//        meta.version = VERSION;
//
//        meta.description = "Provides various Utilities for other mods.";
//
//        meta.autogenerated = false;
