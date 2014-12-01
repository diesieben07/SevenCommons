package de.take_weiland.mods.commons.internal.sync;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
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

	public static final String CLASS_NAME = "de/take_weiland/mods/commons/internal/sync/SyncingManager";
	public static final String BOOTSTRAP = "inDyBootstrap";
	public static final String CREATE_SYNCER = "createSyncer";
	public static final String CREATE_CONTAINER_SYNCER = "createContainerSyncer";

	public static final int METHOD = 0;
	public static final int FIELD = 1;

	private static final ConcurrentMap<Class<?>, Function<? super SyncContext<?>, ValueSyncer<?>>> valueSyncers;

	private static List<SyncerFinder> finders;

	private static final ConcurrentMap<Class<?>, Integer> typeIds = new MapMaker().concurrencyLevel(2).makeMap();

	static {
		MapMaker mm = new MapMaker().concurrencyLevel(2);
		valueSyncers = mm.makeMap();
//		contentSyncers = mm.makeMap();

		finders = Lists.newArrayList();
	}

	public static <T> void registerValueSyncer(Class<T> clazz, Function<SyncContext<? extends T>, ValueSyncer<T>> function) {

	}

	public static <T> void register(Class<T> clazz, ValueSyncer<T> syncer) {
		register(new DirectContext<>(clazz), syncer);
	}

	public static <T> void register(SyncContext<T> context, ValueSyncer<T> syncer) {
		if (valueSyncers.putIfAbsent(context, syncer) != null) {
			throw new IllegalArgumentException("Already have a ValueSyncer for " + context);
		}
	}

	public static <T> void register(Class<T> clazz, ContentSyncer<T> syncer) {
		register(new DirectContext<>(clazz), syncer);
	}

	public static <T> void register(SyncContext<T> context, ContentSyncer<T> syncer) {
		if (contentSyncers.putIfAbsent(context, syncer) != null) {
			throw new IllegalArgumentException("Already have a ContentSyncer for " + context);
		}
	}

	public static <T> void register(SyncContext<T> context, ValueSyncer<T> valueSyncer, ContentSyncer<T> contentSyncer) {
		register(context, valueSyncer);
		register(context, contentSyncer);
	}

	public static <T> void register(Class<T> clazz, ValueSyncer<T> valueSyncer, ContentSyncer<T> contentSyncer) {
		register(new DirectContext<>(clazz), valueSyncer, contentSyncer);
	}

	public static void register(SyncerFinder finder) {
		checkState(!Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION), "Cannot add new Syncers after Init");
		finders.add(finder);
	}

	@SuppressWarnings("unchecked")
	public static <T> ValueSyncer<T> getValueSyncer(SyncContext<T> context) {
		ValueSyncer<T> syncer = (ValueSyncer<T>) valueSyncers.get(context);

		if (syncer == null) {
			for (SyncerFinder finder : finders) {
				if (syncer == null) {
					syncer = finder.findValueSyncer(context);
				} else {
					if (finder.findValueSyncer(context) != null) {
						throw new IllegalStateException("Multiple ValueSyncers for " + context);
					}
				}
			}
			if (syncer == null) {
				throw new IllegalStateException("No ValueSyncer for " + context);
			} else {
				if (valueSyncers.putIfAbsent(context, syncer) != null) {
					syncer = (ValueSyncer<T>) valueSyncers.get(context);
				}
			}
		}
		return syncer;
	}

	@SuppressWarnings("unchecked")
	public static <T> ContentSyncer<T> getContentSyncer(SyncContext<T> context) {
		ContentSyncer<T> syncer = (ContentSyncer<T>) contentSyncers.get(context);

		if (syncer == null) {
			for (SyncerFinder finder : finders) {
				if (syncer == null) {
					syncer = finder.findContentSyncer(context);
				} else {
					if (finder.findContentSyncer(context) != null) {
						throw new IllegalStateException("Multiple ContentSyncers for " + context);
					}
				}
			}
			if (syncer == null) {
				throw new IllegalStateException("No ContentSyncer for " + context);
			} else {
				if (contentSyncers.putIfAbsent(context, syncer) != null) {
					syncer = (ContentSyncer<T>) contentSyncers.get(context);
				}
			}
		}

		return syncer;
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

	private static boolean setup;

	static {
		setup();
	}

	public static void setup() {
		if (setup) return;

		setup = true;
		// primitives and their wrappers are handled via ASM

		register(Item.class, new ItemSyncer());
		register(Block.class, new BlockSyncer());

		register(ItemStack.class, new ItemStackSyncer(), new ItemStackSyncer.Contents());
		register(FluidStack.class, new FluidStackSyncer(), new FluidStackSyncer.Contents());
		register(BitSet.class, new BitSetSyncer(), new BitSetSyncer.Contents());

		FluidTankSyncer.register();

		register(new EnumSetSyncerFinder());
		register(new EnumSyncerFinder());
	}
}
