package de.take_weiland.mods.commons.sync;

import com.google.common.collect.Maps;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.Map;

public final class Syncing {

	private static Map<Class<?>, TypeSyncer<?>> syncers = Maps.newHashMap();

	/**
	 * <p>register a global TypeSyncer to make the given type available for syncing with {@link Sync @Synced}.</p>
	 * <p>Overrides may be specified using {@link Sync#syncer()}</p>
	 * @param clazz the class to register a syncer for
	 * @param syncer the syncer to register
	 */
	public static <T> void registerSyncer(Class<T> clazz, TypeSyncer<T> syncer) {
		syncers.put(clazz, syncer);
	}

	/**
	 * get a TypeSyncer for the given type
	 * @param clazz the class to be synced
	 * @return a TypeSyncer for the given class or null if none
	 */
	@SuppressWarnings("unchecked") // cast is safe as map is guarded by registerSyncer
	public static <T> TypeSyncer<T> getSyncerFor(Class<T> clazz) {
		return (TypeSyncer<T>) syncers.get(clazz);
	}

	static {
		// default built-in syncers
		registerSyncer(String.class, new StringSyncer());
		registerSyncer(FluidStack.class, new FluidStackSyncer());
		registerSyncer(ItemStack.class, new ItemStackSyncer());
	}
	
}
