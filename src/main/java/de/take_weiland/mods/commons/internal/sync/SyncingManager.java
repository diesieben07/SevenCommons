package de.take_weiland.mods.commons.internal.sync;

import com.google.common.collect.*;
import de.take_weiland.mods.commons.internal.PacketTypeID;
import de.take_weiland.mods.commons.internal.PacketTypeIds;
import de.take_weiland.mods.commons.internal.sync.impl.*;
import de.take_weiland.mods.commons.net.ModPacket;
import de.take_weiland.mods.commons.properties.ClassProperty;
import de.take_weiland.mods.commons.sync.*;
import de.take_weiland.mods.commons.util.Players;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class SyncingManager {

	private SyncingManager() {}

	public static final String CLASS_NAME = "de/take_weiland/mods/commons/internal/sync/SyncingManager";
	public static final String BOOTSTRAP = "inDyBootstrap";
	public static final String CREATE_SYNCER = "createSyncer";
	public static final String CREATE_CONTAINER_SYNCER = "createContainerSyncer";

	public static final int METHOD = 0;
	public static final int FIELD = 1;

	private static Multimap<Class<?>, SyncerProvider> valueSyncers;
	private static Multimap<Class<?>, SyncerProvider> contentSyncers;

	private static final ConcurrentMap<Class<?>, Integer> typeIds = new MapMaker().concurrencyLevel(2).makeMap();

	static {
		valueSyncers = ArrayListMultimap.create();
		contentSyncers = ArrayListMultimap.create();
	}

	public static <T> SyncingConfigurator<T> sync(Class<T> clazz) {
		return new ConfiguratorImpl<>(clazz);
	}

	public static void register(Class<?> clazz, SyncerProvider.ForValue provider) {
		valueSyncers.put(clazz, provider);
	}

	public static void register(Class<?> clazz, SyncerProvider.ForContents provider) {
		contentSyncers.put(clazz, provider);
	}

	static void registerInternal(Class<?> clazz, SyncerProvider provider, boolean isContentSyncer) {
		(isContentSyncer ? contentSyncers : valueSyncers).put(clazz, provider);
	}

	public static <T> ValueSyncer<T> getValueSyncer(ClassProperty<T> element) {
		return (ValueSyncer<T>) getSyncer(element, valueSyncers);
	}

	public static <T> ContentSyncer<T> getContentSyncer(ClassProperty<T> element) {
		return (ContentSyncer<T>) getSyncer(element, contentSyncers);
	}

	private static <T> Syncer<T> getSyncer(ClassProperty<T> element, Multimap<Class<?>, ? extends SyncerProvider> syncers) {
		Syncer<T> syncer;
		Class<?> clazz = element.getType().getRawType();

		do {
			syncer = findSyncer(element, clazz, syncers);
			if (syncer != null || clazz == Object.class) {
				break;
			} else {
				clazz = clazz.getSuperclass();
			}
		} while (true);
		if (syncer == null) {
			throw new IllegalStateException("No Syncer found for " + element.getType());
		}
		return syncer;
	}

	private static <T> Syncer<T> findSyncer(ClassProperty<T> element, Class<?> baseClazz, Multimap<Class<?>, ? extends SyncerProvider> syncers) {
		Syncer<T> syncer = null;
		for (SyncerProvider provider : syncers.get(baseClazz)) {
			if (syncer == null) {
				syncer = provider.apply(element);
			} else {
				if (provider.apply(element) != null) {
					throw new IllegalStateException("Multiple Syncers for " + element.getType() + " on level " + baseClazz);
				}
			}
		}
		return syncer;
	}


	public static void freeze() {
		// make it threadsafe
		valueSyncers = ImmutableMultimap.copyOf(valueSyncers);
		contentSyncers = ImmutableMultimap.copyOf(contentSyncers);
	}

	public static int getTypeIdServer(Class<?> clazz) {
		Integer id = typeIds.get(clazz);
		if (id == null) {
			id = clazz.getName().hashCode();
			while (typeIds.putIfAbsent(clazz, id) != null) {
				id++;
			}
		}
		return id;
	}

	public static int getTypeIdClient(Class<?> clazz) {
		Integer id = typeIds.get(clazz);
		if (id == null) {
			throw new RuntimeException("Unknown TypeID on the client, this should not happen!");
		}
		return id;
	}

	public static void onNewID(Class<?> clazz, Integer id) {
		ModPacket packet = new PacketTypeID(clazz, id);

		List<EntityPlayerMP> all = Players.getAll();
		//noinspection ForLoopReplaceableByForEach
		for (int i = 0, len = all.size(); i < len; i++) {
			EntityPlayerMP player = all.get(i);
			if (!Players.isSPOwner(player)) {
				packet.sendTo(player);
			}
		}
	}

	public static void sendIDsTo(EntityPlayer player) {
		if (!Players.isSPOwner(player)) {
			// send current snapshot
			new PacketTypeIds(ImmutableMap.copyOf(typeIds));
		}
	}

	public static void injectTypeIDs(Map<Class<?>, Integer> map) {
		typeIds.clear();
		typeIds.putAll(map);
	}

	public static void injectTypeId(Class<?> clazz, int id) {
		typeIds.put(clazz, id);
	}

	private static boolean setup;

	static {
		setup();
	}

	public static void setup() {
		if (setup) return;

		setup = true;

		sync(Item.class).with(new ItemSyncer());
		sync(Block.class).with(new BlockSyncer());

		sync(ItemStack.class).with(new ItemStackSyncer());
		sync(ItemStack.class).with(new ItemStackSyncer.Contents());

		sync(FluidStack.class).with(new FluidStackSyncer());
		sync(FluidStack.class).with(new FluidStackSyncer.Contents());

		sync(BitSet.class).with(new BitSetSyncer());
		sync(BitSet.class).with(new BitSetSyncer.Contents());

		FluidTankSyncer.register();
		EnumSyncer.register();
		EnumSetSyncer.register();

	}
}
