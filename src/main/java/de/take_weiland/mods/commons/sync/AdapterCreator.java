package de.take_weiland.mods.commons.sync;

import com.google.common.collect.Maps;
import com.google.common.primitives.Primitives;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.Map;
import java.util.UUID;

/**
 * @author diesieben07
 */
public abstract class AdapterCreator<T> {

	public abstract SyncAdapter<T> newInstance();

	public static String CLASS_NAME = "de/take_weiland/mods/commons/sync/AdapterCreator";
	public static String NEW_INSTANCE = "newInstance";

	private static AdapterCreator<String> stringCreator;
	static AdapterCreator<String> forString() {
		if (stringCreator == null) {
			stringCreator = new AdapterCreator<String>() {
				@Override
				public SyncAdapter<String> newInstance() {
					return new StringAdapter();
				}
			};
		}
		return stringCreator;
	}

	private static AdapterCreator<UUID> uuidCreator;
	static AdapterCreator<UUID> forUUID() {
		if (uuidCreator == null) {
			uuidCreator = new AdapterCreator<UUID>() {
				@Override
				public SyncAdapter<UUID> newInstance() {
					return new UUIDAdapter();
				}
			};
		}
		return uuidCreator;
	}


	private static AdapterCreator<Enum<?>> enumCreator;
	@SuppressWarnings("rawtypes")
	static <E extends Enum<E>> AdapterCreator forEnum() {
		if (enumCreator == null) {
			enumCreator = new AdapterCreator<Enum<?>>() {
				@Override
				public SyncAdapter<Enum<?>> newInstance() {
					return new EnumAdapter();
				}
			};
		}
		return enumCreator;
	}

	private static AdapterCreator<ItemStack> itemStackCreator;
	@SuppressWarnings("rawtypes")
	static AdapterCreator forItemStack() {
		if (itemStackCreator == null) {
			itemStackCreator = new AdapterCreator<ItemStack>() {
				@Override
				public SyncAdapter<ItemStack> newInstance() {
					return new ItemStackAdapter();
				}
			};
		}
		return itemStackCreator;
	}

	private static AdapterCreator<FluidStack> fluidStackCreator;
	@SuppressWarnings("rawtypes")
	static AdapterCreator forFluidStack() {
		if (fluidStackCreator == null) {
			fluidStackCreator = new AdapterCreator<FluidStack>() {
				@Override
				public SyncAdapter<FluidStack> newInstance() {
					return new FluidStackAdapter();
				}
			};
		}
		return fluidStackCreator;
	}

	private static Map<Class<?>, AdapterCreator<?>> wrapperCreators;
	static AdapterCreator<?> forPrimitiveWrapper(Class<?> primWrap) {
		if (wrapperCreators == null) {
			wrapperCreators = Maps.newHashMapWithExpectedSize(8);
		}
		AdapterCreator<?> creator = wrapperCreators.get(primWrap);
		if (creator == null) {
			switch (Primitives.unwrap(primWrap).getName()) {
				case "boolean":
					creator = new PrimitiveWrapperAdapter.OfBooleanCreator();
					break;
				case "byte":
					creator = new PrimitiveWrapperAdapter.OfByteCreator();
					break;
				case "short":
					creator = new PrimitiveWrapperAdapter.OfShortCreator();
					break;
				case "int":
					creator = new PrimitiveWrapperAdapter.OfIntCreator();
					break;
				case "long":
					creator = new PrimitiveWrapperAdapter.OfLongCreator();
					break;
				case "float":
					creator = new PrimitiveWrapperAdapter.OfFloatCreator();
					break;
				case "double":
					creator = new PrimitiveWrapperAdapter.OfDoubleCreator();
					break;
				case "char":
					creator = new PrimitiveWrapperAdapter.OfCharCreator();
					break;
				default: // case "void"
					throw new AssertionError();
			}
			wrapperCreators.put(primWrap, creator);
		}
		return creator;
	}

}
