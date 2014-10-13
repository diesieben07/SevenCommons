package de.take_weiland.mods.commons.nbt;

import net.minecraft.nbt.NBTBase;

import javax.annotation.Nullable;

/**
* @author diesieben07
*/
class EnumSerializer<E extends Enum<E>> implements NBTSerializer.NullSafe<E> {
	private final Class<E> clazz;

	public EnumSerializer(Class<E> clazz) {
		this.clazz = clazz;
	}

	@Override
	public NBTBase serialize(@Nullable E instance) {
		return NBT.writeEnum(instance);
	}

	@Override
	public E deserialize(@Nullable NBTBase nbt) {
		return NBT.readEnum(nbt, clazz);
	}
}
