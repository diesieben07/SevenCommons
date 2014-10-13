package de.take_weiland.mods.commons.nbt;

import net.minecraft.nbt.NBTBase;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public interface NBTSerializer<T> {

	NBTBase serialize(T instance);

	T deserialize(NBTBase nbt);

	interface NullSafe<T> extends NBTSerializer<T> {

		@Override
		NBTBase serialize(@Nullable T instance);

		@Override
		T deserialize(@Nullable NBTBase nbt);
	}

}
