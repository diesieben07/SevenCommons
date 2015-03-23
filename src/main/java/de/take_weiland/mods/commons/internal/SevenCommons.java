package de.take_weiland.mods.commons.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.primitives.Primitives;
import cpw.mods.fml.client.FMLFileResourcePack;
import cpw.mods.fml.client.FMLFolderResourcePack;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.internal.client.ClientProxy;
import de.take_weiland.mods.commons.internal.exclude.ClassInfoUtil;
import de.take_weiland.mods.commons.internal.sync.PacketSync;
import de.take_weiland.mods.commons.nbt.BuiltinSerializers;
import de.take_weiland.mods.commons.nbt.NBTSerializers;
import de.take_weiland.mods.commons.net.Network;
import de.take_weiland.mods.commons.net.PacketHandler;
import de.take_weiland.mods.commons.util.Logging;
import net.minecraftforge.common.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class SevenCommons extends DummyModContainer {

	public static final Logger LOGGER = Logging.getLogger("SevenCommons");
	public static final String VERSION = "1.0";
	public static long clientMainThreadID;

	public static SevenCommonsProxy proxy;
	public static SevenCommons instance;

    //	@GetProperty(comment = "Set to false to disable the auto-updating feature of SevenCommons")
	// TODO
	public static boolean updaterEnabled = true;

	public static PacketHandler packets;
	public static final int SYNC_PACKET_ID = 0;

	private static List<Runnable> postInitCallbacks = new ArrayList<>();

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
		proxy = new ClientProxy();
		clientMainThreadID = Thread.currentThread().getId();
		universalPreInit(event);
	}

	@SideOnly(Side.SERVER)
	@Subscribe
	public void serverPreInit(FMLPreInitializationEvent event) {
		proxy = new ServerProxy();
		universalPreInit(event);
	}

	public void universalPreInit(FMLPreInitializationEvent event) {
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());

		packets = Network.newChannel("SevenCommons")
				.register(PacketSync.class, SYNC_PACKET_ID)
				.register(PacketInventoryName.class)
				.register(PacketContainerButton.class)
				.build();

		ClassInfoUtil.preInit();

		TickRegistry.registerTickHandler(new SCPlayerTicker(), Side.SERVER);
		GameRegistry.registerPlayerTracker(new SCPlayerTracker());

		proxy.preInit(event);

        BuiltinSerializers factory = new BuiltinSerializers();
        NBTSerializers.register(Object.class, factory);
        for (Class<?> prim : Primitives.allPrimitiveTypes()) {
            if (prim != void.class) {
                NBTSerializers.register(prim, factory);
            }
        }

//		WatcherRegistry.register(Object.class, new DefaultWatcherSPI());
	}

	@Subscribe
	public void postInit(FMLPostInitializationEvent event) {
		synchronized (SevenCommons.class) {
			for (Runnable callback : postInitCallbacks) {
				callback.run();
			}
			postInitCallbacks = null;
		}
	}

	@Override
	public File getSource() {
		return SevenCommonsLoader.source;
	}

	@Override
	public Class<?> getCustomResourcePackClass() {
		return getSource().isDirectory() ? FMLFolderResourcePack.class : FMLFileResourcePack.class;
	}

	public static void registerPostInitCallback(Runnable callback) {
		synchronized (SevenCommons.class) {
			if (postInitCallbacks == null) {
				callback.run();
			} else {
				postInitCallbacks.add(callback);
			}
		}
	}

}
