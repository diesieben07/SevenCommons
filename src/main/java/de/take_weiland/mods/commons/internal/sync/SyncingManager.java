package de.take_weiland.mods.commons.internal.sync;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.reflect.TypeToken;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import de.take_weiland.mods.commons.internal.PacketTypeId;
import de.take_weiland.mods.commons.internal.PacketTypeIds;
import de.take_weiland.mods.commons.net.ModPacket;
import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.ValueSyncer;
import de.take_weiland.mods.commons.sync.impl.*;
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

import static com.google.common.base.Preconditions.checkState;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class SyncingManager {

	private SyncingManager() {}

	public static <T> ValueSyncer<T> getValueSyncer(TypeToken<T> type) {
		ValueSyncer<T> syncer = null;
		for (SyncerFinder finder : finders) {
			if (syncer == null) {
				syncer = finder.findValueSyncer(type);
			} else {
				if (finder.findValueSyncer(type) != null) {
					throw new IllegalStateException("Multiple ValueSyncers for type " + type);
				}
			}
		}
		if (syncer == null) {
			throw new IllegalStateException("No ValueSyncer for type " + type);
		} else {
			return syncer;
		}
	}

	public static <T> ContentSyncer<T> getContentSyncer(TypeToken<T> type) {
		ContentSyncer<T> syncer = null;
		for (SyncerFinder finder : finders) {
			if (syncer == null) {
				syncer = finder.findContentSyncer(type);
			} else {
				if (finder.findContentSyncer(type) != null) {
					throw new IllegalStateException("Multiple ContentSyncers for type " + type);
				}
			}
		}
		if (syncer == null) {
			throw new IllegalStateException("No ContentSyncer for type " + type);
		} else {
			return syncer;
		}
	}

	public static <T> void registerValueSyncer(Class<T> clazz, ValueSyncer<T> syncer) {
		registerSyncerFinder(new StandardSyncerFinder<>(clazz, syncer));
	}

	public static void registerValueSyncer(Predicate<TypeToken<?>> filter, ValueSyncer<?> syncer) {
		registerSyncerFinder(new SyncerFinderPredicate(filter, syncer));
	}

	public static <T> void registerContentSyncer(Class<T> clazz, ContentSyncer<T> syncer) {
		registerSyncerFinder(new StandardSyncerFinder.Content<>(clazz, syncer));
	}

	public static void registerContentSyncer(Predicate<TypeToken<?>> filter, ContentSyncer<?> syncer) {
		registerSyncerFinder(new SyncerFinderPredicate.Contents(filter, syncer));
	}

	public static void registerSyncerFinder(SyncerFinder finder) {
		checkState(!Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION), "Cannot add new Syncers after Init");
		finders.add(finder);
	}

	public static void freeze() {
		// make it threadsafe
		finders = ImmutableList.copyOf(finders);
	}

	public static final String CLASS_NAME = "de/take_weiland/mods/commons/internal/sync/SyncingManager";
	public static final String BOOTSTRAP = "inDyBootstrap";
	public static final String CREATE_SYNCER = "createSyncer";
	public static final String CREATE_CONTAINER_SYNCER = "createContainerSyncer";

	public static final int METHOD = 0;
	public static final int FIELD = 1;

	private static final ConcurrentMap<Class<?>, Integer> typeIds;

	private static List<SyncerFinder> finders;

	private static int nextTypeId; // doesn't need to be atomic, only server uses it

	static {
		MapMaker mm = new MapMaker().concurrencyLevel(2);
		typeIds = mm.makeMap();

		finders = Lists.newArrayList();
	}

	public static int getTypeIdServer(Class<?> clazz) {
		Integer id = typeIds.get(clazz);
		if (id == null) {
			id = nextTypeId++;
			if (typeIds.putIfAbsent(clazz, id) != null) {
				throw new RuntimeException("Concurrent write access to SyncingManager.typeIds. This is a bug!");
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
		ModPacket packet = new PacketTypeId(clazz, id);

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

	static {
		// primitives and their wrappers are handled via ASM

		registerValueSyncer(Item.class, new ItemSyncer());
		registerValueSyncer(Block.class, new BlockSyncer());

		registerValueSyncer(ItemStack.class, new ItemStackSyncer());
		registerContentSyncer(ItemStack.class, new ItemStackSyncer.Contents());

		registerValueSyncer(FluidStack.class, new FluidStackSyncer());
		registerContentSyncer(FluidStack.class, new FluidStackSyncer.Contents());

		registerValueSyncer(BitSet.class, new BitSetSyncer());
		registerContentSyncer(BitSet.class, new BitSetSyncer.Contents());

		FluidTankSyncer.register();

		registerSyncerFinder(new EnumSyncerFinder());
	}
}
