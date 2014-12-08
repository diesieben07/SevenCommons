package de.take_weiland.mods.commons.nbt;

import net.minecraft.nbt.NBTBase;

import javax.annotation.Nullable;

/**
* @author diesieben07
*/
class NullSafeSerializerWrapper<T> implements NBTSerializer.NullSafe<T> {

	private final NBTSerializer<T> wrapped;

	NullSafeSerializerWrapper(NBTSerializer<T> wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public NBTBase serialize(@Nullable T instance) {
		return instance == null ? NBT.serializedNull() : wrapped.serialize(instance);
	}

	@Override
	public T deserialize(@Nullable NBTBase nbt) {
		return NBT.isSerializedNull(nbt) ? null : wrapped.deserialize(nbt);
	}
}
