package de.take_weiland.mods.commons.sync;

import com.google.common.primitives.Primitives;
import com.google.common.reflect.TypeToken;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.UUID;

/**
 * @author diesieben07
 */
public abstract class SyncAdapter<T> {

	public static final String CLASS_NAME = "de/take_weiland/mods/commons/sync/SyncAdapter";
	public static final String CHECK_AND_UPDATE = "checkAndUpdate";
	public static final String CREATOR = "creatorASMHook";

	public static InstanceCreator<?> creatorASMHook(Object reflElement, boolean isField) throws Exception {
		TypeToken<?> tt;
		if (isField) {
			tt = TypeToken.of(((Field) reflElement).getGenericType());
		} else {
			tt = TypeToken.of(((Method) reflElement).getGenericReturnType());
		}

		return creatorFor(tt);
	}

	@SuppressWarnings("unchecked")
	private static <T> InstanceCreator<? super T> creatorFor(TypeToken<T> tt) throws Exception {
		Class<? super T> rawType = tt.getRawType();
		if (List.class.isAssignableFrom(rawType)) {
			TypeToken<?> listValues = tt.resolveType(List.class.getDeclaredMethod("get", int.class).getGenericReturnType());
			return creatorForLists(rawType, listValues);
		} else if (Iterable.class.isAssignableFrom(rawType)) {
			// TODO
		} else if (Map.class.isAssignableFrom(rawType)) {
			// TODO
		} else {
			return creatorForSimpleClass(rawType);
		}
		throw new RuntimeException("NYI");
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static InstanceCreator creatorForLists(Class<?> list, TypeToken<?> values) throws Exception {
		final InstanceCreator<?> valueAdapter = creatorFor(values);
		if (RandomAccess.class.isAssignableFrom(list)) {
			return new InstanceCreator() {
				@Override
				public SyncAdapter newInstance() {
					return new ListAdapter.ForRandomAccess<>(valueAdapter);
				}
			};
		} else {
			return new InstanceCreator() {
				@Override
				public SyncAdapter newInstance() {
					return new ListAdapter.ForLinked<>(valueAdapter);
				}
			};
		}
	}

	public static InstanceCreator creatorForIterable(Class<?> iterable, TypeToken<?> values) throws Exception {
		final InstanceCreator<?> valueAdapter = creatorFor(values);
		return new InstanceCreator() {
			@Override
			public SyncAdapter newInstance() {
				return new SetAdapter(valueAdapter);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> InstanceCreator<? super T> creatorForSimpleClass(Class<T> clazz) {
		if (clazz == String.class || clazz == UUID.class || Primitives.isWrapperType(clazz)) {
			return InstanceCreator.forImmutable();
		} else if (clazz.isEnum()) {
			return InstanceCreator.forEnum();
		} else if (clazz == ItemStack.class) {
			return InstanceCreator.forItemStack();
		} else if (clazz == FluidStack.class) {
			return InstanceCreator.forFluidStack();
		} else {
			throw new IllegalArgumentException(String.format("Cannot sync class %s", clazz.getName()));
		}
	}

	public abstract boolean checkAndUpdate(T newValue);

}
