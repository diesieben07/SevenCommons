package de.take_weiland.mods.commons.sync;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * @author diesieben07
 */
public abstract class InstanceCreator<T> {

	public static String CLASS_NAME = "de/take_weiland/mods/commons/sync/InstanceCreator";
	public static String NEW_INSTANCE = "newInstance";

	public abstract T newInstance();

	private static InstanceCreator<SyncAdapter<Object>> immutableCreator;
	static InstanceCreator<SyncAdapter<Object>> forImmutable() {
		if (immutableCreator == null) {
			immutableCreator = new InstanceCreator<SyncAdapter<Object>>() {
				@Override
				public SyncAdapter<Object> newInstance() {
					return new ImmutableAdapter<>();
				}
			};
		}
		return immutableCreator;
	}

	private static InstanceCreator<SyncAdapter<Enum<?>>> enumCreator;
	static InstanceCreator<SyncAdapter<Enum<?>>> forEnum() {
		if (enumCreator == null) {
			enumCreator = new InstanceCreator<SyncAdapter<Enum<?>>>() {
				@Override
				public SyncAdapter<Enum<?>> newInstance() {
					return new EnumAdapter();
				}
			};
		}
		return enumCreator;
	}

	private static InstanceCreator<SyncAdapter<ItemStack>> itemStackCreator;
	static InstanceCreator<SyncAdapter<ItemStack>> forItemStack() {
		if (itemStackCreator == null) {
			itemStackCreator = new InstanceCreator<SyncAdapter<ItemStack>>() {
				@Override
				public SyncAdapter<ItemStack> newInstance() {
					return new ItemStackAdapter();
				}
			};
		}
		return itemStackCreator;
	}

	private static InstanceCreator<SyncAdapter<FluidStack>> fluidStackCreator;
	static InstanceCreator<SyncAdapter<FluidStack>> forFluidStack() {
		if (fluidStackCreator == null) {
			fluidStackCreator = new InstanceCreator<SyncAdapter<FluidStack>>() {
				@Override
				public SyncAdapter<FluidStack> newInstance() {
					return new FluidStackAdapter();
				}
			};
		}
		return fluidStackCreator;
	}

}
