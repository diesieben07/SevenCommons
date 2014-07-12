package de.take_weiland.mods.commons.sync;

import com.google.common.primitives.Primitives;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author diesieben07
 */
public abstract class SyncAdapter<T> {

	public static final String CLASS_NAME = "de/take_weiland/mods/commons/sync/SyncAdapter";
	public static final String CHECK_AND_UPDATE = "checkAndUpdate";
	public static final String CREATOR = "creatorASMHook";

	public static InstanceCreator<SyncAdapter<?>> creatorASMHook(Object reflElement, boolean isField) {
		Class<?> syncClass;
		if (isField) {
			syncClass = ((Field) reflElement).getType();
		} else {
			syncClass = ((Method) reflElement).getReturnType();
		}

		if (Iterable.class.isAssignableFrom(syncClass)) {
			// TODO
		} else if (Map.class.isAssignableFrom(syncClass)) {
			// TODO
		} else {
			return ((InstanceCreator<SyncAdapter<?>>) creator(syncClass));
		}
		throw new RuntimeException("NYI");
	}

	@SuppressWarnings("unchecked")
	public static <T> InstanceCreator<SyncAdapter<? super T>> creator(Class<T> clazz) {
		checkArgument(!Iterable.class.isAssignableFrom(clazz), "Use specialized methods for Iterable-Adapters.");
		return (InstanceCreator<SyncAdapter<? super T>>) create0(clazz);
	}

	public static <LIST extends List<V>, V> InstanceCreator<SyncAdapter<? super LIST>> creator(Class<LIST> list, Class<V> values) {
		final InstanceCreator<SyncAdapter<? super V>> valueAdapter = creator(values);
		if (RandomAccess.class.isAssignableFrom(list)) {
			return new InstanceCreator<SyncAdapter<? super LIST>>() {
				@Override
				public SyncAdapter<? super LIST> newInstance() {
					return new ListAdapter.ForRandomAccess<>(valueAdapter);
				}
			};
		} else {
			return new InstanceCreator<SyncAdapter<? super LIST>>() {
				@Override
				public SyncAdapter<? super LIST> newInstance() {
					return new ListAdapter.ForLinked<>(valueAdapter);
				}
			};
		}
	}

	private static Object create0(Class<?> clazz) {
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
