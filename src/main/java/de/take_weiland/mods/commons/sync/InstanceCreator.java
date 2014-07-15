package de.take_weiland.mods.commons.sync;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * @author diesieben07
 */
public abstract class InstanceCreator<T> {

	public static String CLASS_NAME = "de/take_weiland/mods/commons/sync/InstanceCreator";
	public static String NEW_INSTANCE = "newInstance";

	public abstract SyncAdapter<T> newInstance();

	private static InstanceCreator<Object> immutableCreator;
	static <T> InstanceCreator<? super T> forImmutable() {
		if (immutableCreator == null) {
			immutableCreator = new InstanceCreator<Object>() {
				@Override
				public SyncAdapter<Object> newInstance() {
					return new ImmutableAdapter<>();
				}
			};
		}
		return immutableCreator;
	}

	private static InstanceCreator<Enum<?>> enumCreator;
	@SuppressWarnings("rawtypes")
	static <E extends Enum<E>> InstanceCreator forEnum() {
		if (enumCreator == null) {
			enumCreator = new InstanceCreator<Enum<?>>() {
				@Override
				public SyncAdapter<Enum<?>> newInstance() {
					return new EnumAdapter();
				}
			};
		}
		return enumCreator;
	}

	private static InstanceCreator<ItemStack> itemStackCreator;
	@SuppressWarnings("rawtypes")
	static InstanceCreator forItemStack() {
		if (itemStackCreator == null) {
			itemStackCreator = new InstanceCreator<ItemStack>() {
				@Override
				public SyncAdapter<ItemStack> newInstance() {
					return new ItemStackAdapter();
				}
			};
		}
		return itemStackCreator;
	}

	private static InstanceCreator<FluidStack> fluidStackCreator;
	@SuppressWarnings("rawtypes")
	static InstanceCreator forFluidStack() {
		if (fluidStackCreator == null) {
			fluidStackCreator = new InstanceCreator<FluidStack>() {
				@Override
				public SyncAdapter<FluidStack> newInstance() {
					return new FluidStackAdapter();
				}
			};
		}
		return fluidStackCreator;
	}

}
