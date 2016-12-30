package de.take_weiland.mods.commons.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.take_weiland.mods.commons.client.Rendering;
import de.take_weiland.mods.commons.crash.Crashing;
import de.take_weiland.mods.commons.internal.client.ClientProxy;
import de.take_weiland.mods.commons.internal.client.worldview.EmptyEntityRenderer;
import de.take_weiland.mods.commons.internal.client.worldview.ViewEntity;
import de.take_weiland.mods.commons.internal.exclude.ClassInfoSuperCache;
import de.take_weiland.mods.commons.internal.net.NetworkImpl;
import de.take_weiland.mods.commons.internal.sync.SyncEvent;
import de.take_weiland.mods.commons.internal.sync_olds.builtin.BuiltinSyncers;
import de.take_weiland.mods.commons.internal.tonbt.ToNbtFactories;
import de.take_weiland.mods.commons.internal.tonbt.builtin.DefaultNBTSerializers;
import de.take_weiland.mods.commons.internal.worldview.PacketRequestWorldInfo;
import de.take_weiland.mods.commons.internal.worldview.PacketWorldInfo;
import de.take_weiland.mods.commons.net.Network;
import de.take_weiland.mods.commons.sync.Syncing;
import de.take_weiland.mods.commons.util.Logging;
import de.take_weiland.mods.commons.util.Scheduler;
import de.take_weiland.mods.commons.worldview.ClientChunks;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.FMLFileResourcePack;
import net.minecraftforge.fml.client.FMLFolderResourcePack;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public final class SevenCommons extends DummyModContainer {

    public static final Logger log = Logging.getLogger("SevenCommons");

    public static final String VERSION = "1.0";
    public static final String MOD_ID  = "sevencommons";

    public static long clientMainThreadID;

    public static ScheduledExecutorService commonScheduler;

    public static SevenCommonsProxy proxy;
    public static SevenCommons      instance;
    public static File              source;

    static Configuration config;

    private static EnumMap<LoaderState.ModState, List<Runnable>> stateCallbacks = new EnumMap<>(LoaderState.ModState.class);
    private static EnumSet<LoaderState.ModState>                 reachedStates  = EnumSet.noneOf(LoaderState.ModState.class);

    public SevenCommons() {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.name = "SevenCommons";
        meta.modId = MOD_ID;
        meta.authorList = ImmutableList.of("diesieben07");
        meta.version = VERSION;

        meta.description = "Provides various Utilities for other mods.";

        meta.autogenerated = false;

        instance = this;

        source = (File) Launch.blackboard.remove("__sevencommons.source");
    }

    public static void main(String[] args) {
        Rendering.drawColoredQuad(0, 0, 100, 100, 0xFF00FF, 128, -2);
    }

    public static Logger scLogger(String channel) {
        return Logging.getLogger("SC|" + channel);
    }

    @Override
    public String getGuiClassName() {
        return "de.take_weiland.mods.commons.internal.SCConfigGui$Factory";
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Subscribe
    public void clientPreInit(FMLPreInitializationEvent event) {
        try {
            proxy = new ClientProxy();
            clientMainThreadID = Thread.currentThread().getId();

            RenderingRegistry.registerEntityRenderingHandler(ViewEntity.class, EmptyEntityRenderer::new);

            universalPreInit(event);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @SideOnly(Side.SERVER)
    @Subscribe
    public void serverPreInit(FMLPreInitializationEvent event) {
        try {
            proxy = new ServerProxy();
            universalPreInit(event);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void universalPreInit(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();

        syncConfig(true);

        BuilderLeakDetect.init();

        MinecraftForge.EVENT_BUS.register(new ForgeEventHandler());

        Network.newSimpleChannel("SevenCommons")
                .register(0, PacketContainerButton::new, PacketContainerButton::handle)
                .register(1, PacketInventoryName::new, PacketInventoryName::handle)
                .register(3, PacketRequestWorldInfo::new, PacketWorldInfo::new, PacketRequestWorldInfo::handle)
                .build();

        NetworkImpl.register(SyncEvent.CHANNEL, (channel, data, side, manager) -> {
            if (side == Network.CLIENT) {
                Scheduler.client().execute(() -> {
                    SyncEvent.handle(data);
                    return false;
                });
            }
        });


        ClassInfoSuperCache.preInit();

        proxy.preInit(event);

        Syncing.registerFactory(Object.class, new BuiltinSyncers());
        ToNbtFactories.registerFactory(Object.class, new DefaultNBTSerializers());

        if (config.hasChanged()) {
            config.save();
        }
    }

    static void syncConfig(boolean initialStartup) {
        int usernameCacheSize = config.getInt("usernameCacheSize", Configuration.CATEGORY_GENERAL, 500, 0, Integer.MAX_VALUE,
                "How many UUID->Username mappings should be kept in the cache, set to 0 to never cache (this is not recommended!)");
        UsernameCache.initCache(usernameCacheSize);

        Property minCorePoolSize = config.get(Configuration.CATEGORY_GENERAL, "commonPoolMinSize", -1,
                "How many Threads to keep alive in the core async-pool even when idle, -1 for default (usually number of cores on the machine).",
                -1, Integer.MAX_VALUE);
        minCorePoolSize.setRequiresMcRestart(true);

        if (initialStartup) {
            startCorePool(minCorePoolSize.getInt());
        }
    }

    private static void startCorePool(int size) {
        int corePoolSize = size < 0 ? Runtime.getRuntime().availableProcessors() : size;

        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("SevenCommonsPool %s")
                .setDaemon(true)
                .setUncaughtExceptionHandler(Crashing.mainThreadExceptionHandler())
                .build();

        commonScheduler = new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                commonScheduler.shutdown();
                if (!commonScheduler.isTerminated()) {
                    log.info("Waiting max. 20 seconds for common thread pool to shut down");
                    try {
                        commonScheduler.awaitTermination(20, TimeUnit.SECONDS);
                        log.info("Common thread pool gracefully shut down");
                    } catch (Throwable x) {
                        log.error("Exception occurred awaiting common thread pool shut down", x);
                    }
                }
            }
        });
    }

    @Override
    public File getSource() {
        return source;
    }

    @Override
    public Class<?> getCustomResourcePackClass() {
        return getSource().isDirectory() ? FMLFolderResourcePack.class : FMLFileResourcePack.class;
    }

    @Subscribe
    public void onServerStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandBase() {
            @Override
            public String getCommandName() {
                return "loadchunk";
            }

            @Override
            public String getCommandUsage(ICommandSender sender) {
                return "loadchunk <dim> <x>, <z>, <radius> [\"un\"]";
            }

            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
                if (args.length != 4 && args.length != 5) {
                    throw new CommandException("Invalid number of args");
                }

                int dim = parseInt(args[0]);
                int x = parseInt(args[1]);
                int z = parseInt(args[2]);
                int r = parseInt(args[3]);
                boolean un = args.length == 5 && args[4].equalsIgnoreCase("un");

                EntityPlayerMP player = getCommandSenderAsPlayer(sender);
                for (int x0 = x - r; x0 <= x + r; x0++) {
                    for (int z0 = z - r; z0 <= z + r; z0++) {
                        if (un) {
                            ClientChunks.unloadChunk(player, dim, x0, z0);
                        } else {
                            ClientChunks.loadChunk(player, dim, x0, z0);
                        }
                    }
                }
            }
        });

        event.registerServerCommand(new CommandBase() {
            @Override
            public String getCommandName() {
                return "blockupdate";
            }

            @Override
            public String getCommandUsage(ICommandSender sender) {
                return "blockupdate <x>, <y>, <z>";
            }

            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
                if (args.length != 3) {
                    throw new CommandException("Invalid number of args");
                }

                int x = parseInt(args[0]);
                int y = parseInt(args[1]);
                int z = parseInt(args[2]);

//                getCommandSenderAsPlayer(sender).worldObj.notifyBlockUpdate(x, y, z);
            }
        });

    }

    @Subscribe
    public void doStateCallback(FMLStateEvent event) {
        synchronized (SevenCommons.class) {
            if (stateCallbacks != null) {
                stateCallbacks.getOrDefault(event.getModState(), Collections.emptyList())
                        .forEach(Runnable::run);
                reachedStates.add(event.getModState());
                if (event.getModState() == LoaderState.ModState.POSTINITIALIZED) {
                    stateCallbacks = null;
                    reachedStates = null;
                }
            }
        }
    }

    public static void registerStateCallback(LoaderState.ModState state, Runnable callback) {
        synchronized (SevenCommons.class) {
            if (stateCallbacks == null || reachedStates.contains(state)) {
                callback.run();
            } else {
                stateCallbacks.computeIfAbsent(state, (key) -> new ArrayList<>())
                        .add(callback);
            }
        }
    }
}
