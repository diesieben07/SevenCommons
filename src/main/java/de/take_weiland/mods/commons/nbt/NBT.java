package de.take_weiland.mods.commons.nbt;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import de.take_weiland.mods.commons.internal.NBTSerialization;
import de.take_weiland.mods.commons.internal.SerializerUtil;
import de.take_weiland.mods.commons.util.SCReflector;
import net.minecraft.nbt.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class NBT {

	public static final int TAG_END = 0;
	public static final int TAG_BYTE = 1;
	public static final int TAG_SHORT = 2;
	public static final int TAG_INT = 3;
	public static final int TAG_LONG = 4;
	public static final int TAG_FLOAT = 5;
	public static final int TAG_DOUBLE = 6;
	public static final int TAG_BYTE_ARR = 7;
	public static final int TAG_STRING = 8;
	public static final int TAG_LIST = 9;
	public static final int TAG_COMPOUND = 10;
	public static final int TAG_INT_ARR = 11;

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

	public static NBTBase writeUUID(UUID uuid) {
		if (uuid == null) {
			return NBTSerialization.serializedNull();
		} else {
			return NBTSerialization.writeUUID(uuid);
		}
	}

	public static void writeUUID(UUID uuid, @NotNull NBTTagCompound nbt, @NotNull String key) {
		nbt.setTag(key, writeUUID(uuid));
	}

	public static UUID readUUID(NBTBase nbt) {
		if (NBTSerialization.isSerializedNull(nbt) || nbt.getId() != TAG_LIST) {
			return null;
		} else {
			return NBTSerialization.readUUID(nbt);
		}
	}

	public static UUID readUUID(@NotNull NBTTagCompound nbt, @NotNull String key) {
		return readUUID(nbt.getTag(key));
	}

	public static NBTBase serialize(@Nullable NBTSerializable serializable) {
		if (serializable == null) {
			return NBTSerialization.serializedNull();
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
			if (NBTSerialization.isSerializedNull(nbt)) {
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
