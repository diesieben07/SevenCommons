package de.take_weiland.mods.commons.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.reflect.Reflection;
import cpw.mods.fml.client.FMLFileResourcePack;
import cpw.mods.fml.client.FMLFolderResourcePack;
import cpw.mods.fml.common.*;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLStateEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.internal.client.ClientProxy;
import de.take_weiland.mods.commons.internal.exclude.ClassInfoUtil;
import de.take_weiland.mods.commons.internal.sync.SyncCodec;
import de.take_weiland.mods.commons.internal.sync.builtin.BuiltinSyncers;
import de.take_weiland.mods.commons.internal.tonbt.ToNbtFactories;
import de.take_weiland.mods.commons.internal.tonbt.builtin.DefaultNBTSerializers;
import de.take_weiland.mods.commons.net.Network;
import de.take_weiland.mods.commons.sync.Syncing;
import de.take_weiland.mods.commons.util.Logging;
import de.take_weiland.mods.commons.util.Scheduler;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

public final class SevenCommons extends DummyModContainer {

    public static final Logger LOGGER = Logging.getLogger("SevenCommons");
    public static final String VERSION = "1.0";
    public static long clientMainThreadID;

    public static SevenCommonsProxy proxy;
    public static SevenCommons instance;

    //	@GetProperty(comment = "Set to false to disable the auto-updating feature of SevenCommons")
    // TODO
    public static boolean updaterEnabled = true;

    public static final int SYNC_PACKET_ID = 0;

    public static SyncCodec syncCodec;

    private static EnumMap<LoaderState.ModState, List<Runnable>> stateCallbacks = new EnumMap<>(LoaderState.ModState.class);
    private static EnumSet<LoaderState.ModState> reachedStates = EnumSet.noneOf(LoaderState.ModState.class);

    public SevenCommons() {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.name = "SevenCommons";
        meta.modId = "sevencommons";
        meta.authorList = ImmutableList.of("diesieben07");
        meta.version = VERSION;

        meta.description = "Provides various Utilities for other mods.";

        meta.autogenerated = false;

        instance = this;
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
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());

        Network.newSimpleChannel("SevenCommons")
                .register(0, PacketContainerButton::new, PacketContainerButton::handle)
                .register(1, PacketInventoryName::new, PacketInventoryName::handle)
                .build();

        syncCodec = new SyncCodec();
        Network.newChannel(syncCodec);


//		packets = Network.newChannel("SevenCommons")
//				.register(PacketSync.class, SYNC_PACKET_ID)
//				.register(PacketInventoryName.class)
//				.register(PacketContainerButton.class)
//				.build();

        ClassInfoUtil.preInit();

        // initialize the lazy statics in the scheduler class
        Reflection.initialize(Scheduler.class);

        FMLCommonHandler.instance().bus().register(new FMLEventHandler());

        proxy.preInit(event);

        Syncing.registerFactory(Object.class, new BuiltinSyncers());
        ToNbtFactories.registerFactory(Object.class, new DefaultNBTSerializers());
    }

    @Override
    public File getSource() {
        return SevenCommonsLoader.source;
    }

    @Override
    public Class<?> getCustomResourcePackClass() {
        return getSource().isDirectory() ? FMLFolderResourcePack.class : FMLFileResourcePack.class;
    }

    @Subscribe
    public void doStateCallback(FMLStateEvent event) {
        synchronized (SevenCommons.class) {
            if (stateCallbacks != null) {
                List<Runnable> list = stateCallbacks.remove(event.getModState());
                if (list != null) {
                    for (Runnable runnable : list) {
                        runnable.run();
                    }
                }
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
                List<Runnable> list = stateCallbacks.get(state);
                if (list == null) {
                    stateCallbacks.put(state, list = new ArrayList<>());
                }
                list.add(callback);
            }
        }
    }

}
