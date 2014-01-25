package de.take_weiland.mods.commons.sync;

import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import com.google.common.collect.Maps;

public final class Syncing {

	private static Map<Class<?>, TypeSyncer<?>> syncers = Maps.newHashMap();
	
	public static <T> void registerSyncer(Class<T> clazz, TypeSyncer<? super T> syncer) {
		syncers.put(clazz, syncer);
	}
	
	@SuppressWarnings("unchecked") // cast is safe as map is guarded by registerSyncer
	public static <T> TypeSyncer<? super T> getSyncerFor(Class<T> clazz) {
		return (TypeSyncer<? super T>) syncers.get(clazz);
	}
	
	static {
		registerSyncer(String.class, new StringSyncer());
		registerSyncer(FluidStack.class, new FluidStackSyncer());
		registerSyncer(ItemStack.class, new ItemStackSyncer());
		registerSyncer(FluidTank.class, new FluidTankSyncer());
	}
	
}
