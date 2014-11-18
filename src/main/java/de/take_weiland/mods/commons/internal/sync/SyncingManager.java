package de.take_weiland.mods.commons.internal.sync;

import com.google.common.collect.MapMaker;
import de.take_weiland.mods.commons.internal.sync.impl.*;
import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.ValueSyncer;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.BitSet;
import java.util.EnumSet;
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

	private static final ConcurrentMap<Class<?>, ValueSyncer<?>> valueSyncers;
	private static final ConcurrentMap<Class<?>, ContentSyncer<?>> contentSyncers;

	static {
		MapMaker mm = new MapMaker().concurrencyLevel(2);
		valueSyncers = mm.makeMap();
		contentSyncers = mm.makeMap();
	}

	public static <T> ValueSyncer<T> getValueSyncer(Class<T> clazz) {
		@SuppressWarnings("unchecked")
		ValueSyncer<T> syncer = (ValueSyncer<T>) valueSyncers.get(clazz);
		if (syncer == null) {
			throw new IllegalArgumentException(String.format("No ValueSyncer for class %s", clazz.getName()));
		}
		return syncer;
	}

	@Nonnull
	public static <T> ContentSyncer<T> getContentSyncer(Class<T> clazz) {
		@SuppressWarnings("unchecked")
		ContentSyncer<T> syncer = (ContentSyncer<T>) contentSyncers.get(clazz);
		if (syncer == null) {
			throw new IllegalArgumentException(String.format("No ContentSyncer for class %s", clazz.getName()));
		}
		return syncer;
	}

	public static <T> void registerValueSyncer(Class<T> clazz, ValueSyncer<T> syncer) {
		if (valueSyncers.putIfAbsent(clazz, syncer) != null) {
			throw new IllegalArgumentException(String.format("ValueSyncer for class %s already registered", clazz.getName()));
		}
	}

	public static <T> void registerContentSyncer(Class<T> clazz, ContentSyncer<T> syncer) {
		if (contentSyncers.putIfAbsent(clazz, syncer) != null) {
			throw new IllegalArgumentException(String.format("ContentSyncer for class %s already registered", clazz.getName()));
		}
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

		registerValueSyncer(Enum.class, new EnumSyncer());

		registerValueSyncer(EnumSet.class, new EnumSetSyncer());
		registerContentSyncer(EnumSet.class, new EnumSetSyncer.Contents());

		FluidTankSyncer.register();
	}
}
