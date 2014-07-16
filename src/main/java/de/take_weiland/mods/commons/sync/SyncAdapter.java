package de.take_weiland.mods.commons.sync;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Primitives;
import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.PacketBuilder;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
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

	public static AdapterCreator<?> creatorASMHook(Object reflElement, boolean isField) throws Exception {
		TypeToken<?> tt;
		if (isField) {
			tt = TypeToken.of(((Field) reflElement).getGenericType());
		} else {
			tt = TypeToken.of(((Method) reflElement).getGenericReturnType());
		}

		return creatorFor(tt);
	}

	@SuppressWarnings("unchecked")
	private static <T> AdapterCreator<? super T> creatorFor(TypeToken<T> tt) {
		Class<? super T> rawType = tt.getRawType();
		if (List.class.isAssignableFrom(rawType)) {
			TypeToken<?> listValues = tt.resolveType(listValueType());
			return creatorForLists(rawType, listValues);
		} else if (Map.class.isAssignableFrom(rawType)) {
			return creatorForMaps(rawType, tt.resolveType(mapKeyType()), tt.resolveType(mapValueType()));
		} else {
			return creatorForSimpleClass(rawType);
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static AdapterCreator creatorForLists(Class<?> list, TypeToken<?> values) {
		if (ImmutableList.class.isAssignableFrom(list)) {
			throw new IllegalArgumentException("Cannot sync an ImmutableCollection");
		}
		final AdapterCreator<?> valueAdapter = creatorFor(values);
		if (RandomAccess.class.isAssignableFrom(list)) {
			return new AdapterCreator() {
				@Override
				public SyncAdapter newInstance() {
					return new RandomAccessListAdapter(valueAdapter);
				}
			};
		} else {
			return new AdapterCreator() {
				@Override
				public SyncAdapter newInstance() {
					return new NonRandomAccessListAdapter(valueAdapter);
				}
			};
		}
	}

	@SuppressWarnings("rawtypes")
	public static AdapterCreator creatorForMaps(Class<?> map, TypeToken<?> keys, TypeToken<?> values) {
		if (ImmutableMap.class.isAssignableFrom(map)) {
			throw new IllegalArgumentException("Cannot sync an ImmutableMap");
		}
		final AdapterCreator<?> keyAdapter = creatorFor(keys);
		final AdapterCreator<?> valueAdapter = creatorFor(values);
		throw new UnsupportedOperationException("Syncing Maps not yet supported");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <T> AdapterCreator creatorForSimpleClass(Class<T> clazz) {
		if (clazz == String.class) {
			return AdapterCreator.forString();
		} else if (clazz == UUID.class) {
			return AdapterCreator.forUUID();
		} else if (Primitives.isWrapperType(clazz)) {
			return AdapterCreator.forPrimitiveWrapper(clazz);
		} else if (clazz.isEnum()) {
			return AdapterCreator.forEnum();
		} else if (clazz == ItemStack.class) {
			return AdapterCreator.forItemStack();
		} else if (clazz == FluidStack.class) {
			return AdapterCreator.forFluidStack();
		} else {
			throw new IllegalArgumentException(String.format("Cannot sync class %s", clazz.getName()));
		}
	}

	private static Type listValueType;
	private static Type listValueType() {
		if (listValueType == null) {
			try {
				listValueType = List.class.getDeclaredMethod("get", int.class).getGenericReturnType();
			} catch (Exception e) {
				throw new Error("List class is missing get(int) method", e);
			}
		}
		return listValueType;
	}

	private static Type mapValueType;
	private static Type mapValueType() {
		if (mapValueType == null) {
			try {
				mapValueType = Map.class.getDeclaredMethod("get", Object.class).getGenericReturnType();
			} catch (Exception e) {
				throw new Error("List class is missing get(int) method", e);
			}
		}
		return mapValueType;
	}

	private static Type mapKeyType;
	private static Type mapKeyType() {
		if (mapKeyType == null) {
			try {
				mapKeyType = Map.class.getDeclaredMethod("put", Object.class, Object.class).getGenericParameterTypes()[1];
			} catch (Exception e) {
				throw new Error("List class is missing get(int) method", e);
			}
		}
		return mapKeyType;
	}

	public abstract boolean checkAndUpdate(T newValue);

	public abstract void write(T value, PacketBuilder builder);

	public abstract <ACTUAL_T extends T> ACTUAL_T read(ACTUAL_T prevVal, DataBuf buf);

}
