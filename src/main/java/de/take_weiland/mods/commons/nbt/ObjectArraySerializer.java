package de.take_weiland.mods.commons.nbt;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Array;

import static de.take_weiland.mods.commons.nbt.NBTData.isSerializedNull;
import static de.take_weiland.mods.commons.nbt.NBTData.serializedNull;

/**
 * @author diesieben07
 */
abstract class ObjectArraySerializer<T> implements NBTSerializer.NullSafe<T[]> {

	@Override
	public NBTBase serialize(@Nullable T[] instance) {
		if (instance == null) {
			return serializedNull();
		} else {
			NBTTagList list = new NBTTagList();
			for (T t : instance) {
				list.appendTag(serialize0(t));
			}
			return list;
		}
	}

	@Override
	public T[] deserialize(@Nullable NBTBase nbt) {
		if (isSerializedNull(nbt)) {
			return null;
		} else {
			NBTTagList list = (NBTTagList) nbt;
			int len = list.tagCount();

			T[] result = newArray(len);
			for (int i = 0; i < len; i++) {
				result[i] = deserialize0(list.tagAt(i));
			}
			return result;
		}
	}

	abstract T[] newArray(int len);
	abstract NBTBase serialize0(@Nullable T element);
	abstract T deserialize0(@Nonnull NBTBase nbt);

	static class Simple<T> extends ObjectArraySerializer<T> {

		private final NullSafe<T> valueSerializer;
		private final Class<T> elementType;

		@SuppressWarnings("unchecked")
		Simple(NullSafe<?> valueSerializer, Class<?> elementType) {
			// unsafe casts are here to simplify creation code
			// we make sure to only pass in correct values
			this.valueSerializer = (NullSafe<T>) valueSerializer;
			this.elementType = (Class<T>) elementType;
		}

		@SuppressWarnings("unchecked")
		@Override
		T[] newArray(int len) {
			return (T[]) Array.newInstance(elementType, len);
		}

		@Override
		NBTBase serialize0(@Nullable T element) {
			return valueSerializer.serialize(element);
		}

		@Override
		T deserialize0(@Nonnull NBTBase nbt) {
			return valueSerializer.deserialize(nbt);
		}
	}

	static class ItemStackSpecialized extends ObjectArraySerializer<ItemStack> {

		@Override
		ItemStack[] newArray(int len) {
			return new ItemStack[len];
		}

		@Override
		NBTBase serialize0(ItemStack element) {
			return NBTData.writeItemStack(element);
		}

		@Override
		ItemStack deserialize0(@Nonnull NBTBase nbt) {
			return NBTData.readItemStack(nbt);
		}
	}

	static class StringSpecialized extends ObjectArraySerializer<String> {

		@Override
		String[] newArray(int len) {
			return new String[len];
		}

		@Override
		NBTBase serialize0(@Nullable String element) {
			return NBTData.writeString(element);
		}

		@Override
		String deserialize0(@Nonnull NBTBase nbt) {
			return NBTData.readString(nbt);
		}
	}

}
