package de.take_weiland.mods.commons.nbt;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import de.take_weiland.mods.commons.util.JavaUtils;
import de.take_weiland.mods.commons.util.SCReflector;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class NBT {

	private NBT() {
	}

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

	public static void nullableWrite(NBTTagCompound nbt, DataOutput out) throws IOException {
		if (nbt == null) {
			out.writeByte(-1);
		} else {
			for (NBTBase tag : asMap(nbt).values()) {
				NBTBase.writeNamedTag(tag, out);
			}
			out.writeByte(0);
		}
	}

	public static NBTTagCompound nullableRead(DataInput in) throws IOException {
		int id = in.readByte();
		if (id == -1) {
			return null;
		} else {
			NBTTagCompound nbt = new NBTTagCompound();
			Map<String, NBTBase> map = asMap(nbt);
			while (id != 0) {
				NBTBase tag = NBTBase.func_130104_b(in, 1);
				map.put(tag.getName(), tag);
				id = in.readByte();
			}
			return nbt;
		}
	}

	public static Function<NBTTagString, String> getStringFunction() {
		return NbtStringDataFunction.INSTANCE;
	}

	private static final Map<Class<?>, NBTSerializer<?>> serializers = Maps.newHashMap();

	public static <T> void registerSerializer(Class<T> toSerialize, NBTSerializer<? super T> serializer) {
		serializers.put(toSerialize, serializer);
	}

	public static <T> NBTSerializer<? super T> getSerializer(T toSerialize) {
		return (NBTSerializer<? super T>) getSerializer(toSerialize.getClass());
	}

	public static <T> NBTSerializer<? super T> getSerializer(Class<T> toSerialize) {
		@SuppressWarnings("unchecked")
		NBTSerializer<? super T> instance = (NBTSerializer<? super T>) serializers.get(toSerialize);
		if (instance == null) {
			return DefaultSerializer.INSTANCE;
		}
		return instance;
	}

	public static <T> NBTTagCompound serialize(T toSerialize) {
		return getSerializer(toSerialize).serialize(toSerialize);
	}

	private static enum DefaultSerializer implements NBTSerializer<Object> {
		INSTANCE;

		@Override
		public NBTTagCompound serialize(Object instance) {
			NBTTagCompound nbt = new NBTTagCompound();


			return nbt;
		}

		@Override
		public Object deserialize(NBTTagCompound nbt) {
			return null;
		}
	}

	private static enum NbtStringDataFunction implements Function<NBTTagString, String> {

		INSTANCE;

		@Override
		public String apply(NBTTagString input) {
			return input.data;
		}

	}

	static {
		registerSerializer(Class.class, new NBTSerializer<Class>() {
			@Override
			public NBTTagCompound serialize(Class instance) {
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("class", instance.getName());
				return nbt;
			}

			@Override
			public Class deserialize(NBTTagCompound nbt) {
				String name = nbt.getString("class");
				try {
					return name.isEmpty() ? null : Class.forName(name);
				} catch (ClassNotFoundException e) {
					throw JavaUtils.throwUnchecked(e);
				}
			}
		});

	}

}
