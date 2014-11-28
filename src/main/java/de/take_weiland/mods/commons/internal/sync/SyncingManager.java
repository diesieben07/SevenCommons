package de.take_weiland.mods.commons.internal.sync;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import de.take_weiland.mods.commons.internal.PacketTypeID;
import de.take_weiland.mods.commons.internal.PacketTypeIds;
import de.take_weiland.mods.commons.net.ModPacket;
import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.ValueSyncer;
import de.take_weiland.mods.commons.sync.ctx.DirectContext;
import de.take_weiland.mods.commons.sync.ctx.SyncContext;
import de.take_weiland.mods.commons.sync.impl.*;
import de.take_weiland.mods.commons.util.Players;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;
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

	private static final ConcurrentMap<Class<?>, Integer> typeIds = new MapMaker().concurrencyLevel(2).makeMap();

	private static List<SyncerFinder> finders = Lists.newArrayList();

	public static ValueSyncer<?> getValueSyncer(Type type) {
		return getValueSyncer(new DirectContext<>(type, ImmutableMap.<SyncContext.Key<?>, Object>of()));
	}

	public static <T> ValueSyncer<T> getValueSyncer(SyncContext<T> context) {
		ValueSyncer<T> syncer = null;
		for (SyncerFinder finder : finders) {
			if (syncer == null) {
				syncer = finder.findValueSyncer(context);
			} else {
				if (finder.findValueSyncer(context) != null) {
					throw new IllegalStateException("Multiple ValueSyncers for type " + context);
				}
			}
		}
		if (syncer == null) {
			throw new IllegalStateException("No ValueSyncer for type " + context);
		} else {
			return syncer;
		}
	}

	public static ContentSyncer<?> getContentSyncer(Type type) {
		return getContentSyncer(new DirectContext<>(type, ImmutableMap.<SyncContext.Key<?>, Object>of()));
	}

	public static <T> ContentSyncer<T> getContentSyncer(SyncContext<T> context) {
		ContentSyncer<T> syncer = null;
		for (SyncerFinder finder : finders) {
			if (syncer == null) {
				syncer = finder.findContentSyncer(context);
			} else {
				if (finder.findContentSyncer(context) != null) {
					throw new IllegalStateException("Multiple ContentSyncers for type " + context);
				}
			}
		}
		if (syncer == null) {
			throw new IllegalStateException("No ContentSyncer for type " + context);
		} else {
			return syncer;
		}
	}

	public static <T> void registerValueSyncer(Class<T> clazz, ValueSyncer<T> syncer) {
		registerSyncerFinder(new StandardSyncerFinder<>(clazz, syncer));
	}

	public static void registerValueSyncer(Predicate<SyncContext<?>> filter, ValueSyncer<?> syncer) {
		registerSyncerFinder(new SyncerFinderPredicate(filter, syncer));
	}

	public static <T> void registerContentSyncer(Class<T> clazz, ContentSyncer<T> syncer) {
		registerSyncerFinder(new StandardSyncerFinder.Content<>(clazz, syncer));
	}

	public static void registerContentSyncer(Predicate<SyncContext<?>> filter, ContentSyncer<?> syncer) {
		registerSyncerFinder(new SyncerFinderPredicate.Contents(filter, syncer));
	}

	public static <T> void registerSyncers(Class<T> clazz, ValueSyncer<T> valueSyncer, ContentSyncer<T> contentSyncer) {
		registerSyncerFinder(new DualFinder<>(clazz, valueSyncer, contentSyncer));
	}

	public static void registerSyncers(Predicate<SyncContext<?>> filter, ValueSyncer<?> valueSyncer, ContentSyncer<?> contentSyncer) {
		registerSyncerFinder(new SyncerFinderPredicate.Dual(filter, valueSyncer, contentSyncer));
	}

	public static void registerSyncerFinder(SyncerFinder finder) {
//		checkState(!Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION), "Cannot add new Syncers after Init");
		finders.add(finder);
	}

	public static void freeze() {
		// make it threadsafe
		finders = ImmutableList.copyOf(finders);
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

	public static void setup() {
		// primitives and their wrappers are handled via ASM

		registerValueSyncer(Item.class, new ItemSyncer());
		registerValueSyncer(Block.class, new BlockSyncer());

		registerSyncers(ItemStack.class, new ItemStackSyncer(), new ItemStackSyncer.Contents());
		registerSyncers(FluidStack.class, new FluidStackSyncer(), new FluidStackSyncer.Contents());
		registerSyncers(BitSet.class, new BitSetSyncer(), new BitSetSyncer.Contents());

		FluidTankSyncer.register();

		registerSyncerFinder(new EnumSetSyncerFinder());
		registerSyncerFinder(new EnumSyncerFinder());
	}
}
