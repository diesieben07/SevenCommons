package de.take_weiland.mods.commons.nbt;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import de.take_weiland.mods.commons.internal.SerializerUtil;
import de.take_weiland.mods.commons.util.SCReflector;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public final class NBT {

	/**
	 * view the given NBTTagList as a {@link List}<br>
	 * the type parameter T can be used if you are sure that this list only contains NBT-Tags of the given type
	 *
	 * @param nbtList the list to view
	 * @return a modifiable list view of the NBTTagList
	 */
	public static <T extends NBTBase> List<T> asList(NBTTagList nbtList) {
		return SCReflector.instance.getWrappedList(nbtList);
	}

	public static Map<String, NBTBase> asMap(NBTTagCompound nbt) {
		return SCReflector.instance.getWrappedMap(nbt);
	}

	public static NBTTagCompound getOrCreateCompound(NBTTagCompound parent, String key) {
		if (!parent.hasKey(key)) {
			parent.setCompoundTag(key, new NBTTagCompound());
		}
		return parent.getCompoundTag(key);
	}

	public static NBTTagList getOrCreateList(NBTTagCompound parent, String key) {
		if (!parent.hasKey(key)) {
			parent.setTag(key, new NBTTagList());
		}
		return parent.getTagList(key);
	}

	@SuppressWarnings("unchecked")
	public static <T extends NBTBase> T copy(T nbt) {
		return nbt == null ? null : (T) nbt.copy();
	}

	private static final byte NULL = -1;
	private static final int NBT_COMPOUND_ID = 10;
	private static final String NULL_KEY = "_sc$null";

	public static NBTBase serialize(@Nullable NBTSerializable serializable) {
		if (serializable == null) {
			NBTTagCompound result = new NBTTagCompound();
			result.setByte(NULL_KEY, NULL);
			return result;
		} else {
			return serializable.serialize();
		}
	}

	public static <T extends NBTSerializable> T deserialize(@NotNull Class<T> clazz, @NotNull NBTBase nbt) {
		return serializer(clazz).deserialize(nbt);
	}

	private static Map<Class<?>, NBTSerializer<?>> serializers;

	public static <T extends NBTSerializable> NBTSerializer<T> serializer(@NotNull Class<T> clazz) {
		if (serializers == null) {
			serializers = Maps.newHashMap();
		}
		@SuppressWarnings("unchecked")
		NBTSerializer<T> serializer = (NBTSerializer<T>) serializers.get(clazz);
		if (serializer == null) {
			serializers.put(clazz, (serializer = compileSerializer(clazz)));
		}
		return serializer;
	}

	private static <T extends NBTSerializable> NBTSerializer<T> compileSerializer(Class<T> clazz) {
		return new SerializerWrapper<>(SerializerUtil.findDeserializer(clazz, NBTSerializable.Deserializer.class, NBTBase.class));
	}

	private static final class SerializerWrapper<T extends NBTSerializable> implements NBTSerializer<T> {

		private final Method deserializer;

		SerializerWrapper(Method deserializer) {
			this.deserializer = deserializer;
		}

		@Override
		public NBTBase serialize(T instance) {
			return NBT.serialize(instance);
		}

		@SuppressWarnings("unchecked")
		@Override
		public T deserialize(NBTBase nbt) {
			if (nbt.getId() == NBT_COMPOUND_ID && ((NBTTagCompound) nbt).getByte(NULL_KEY) == NULL) {
				return null;
			} else {
				try {
					return (T) deserializer.invoke(null, nbt);
				} catch (Exception e) {
					throw Throwables.propagate(e);
				}
			}
		}
	}

	private NBT() { }

}
