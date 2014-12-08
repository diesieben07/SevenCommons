package de.take_weiland.mods.commons.nbt;

import net.minecraft.nbt.NBTBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author diesieben07
 */
public interface NBTSerializer<T> {

	NBTBase serialize(T instance);

	T deserialize(NBTBase nbt);

	interface NullSafe<T> extends NBTSerializer<T> {

		@Override
		NBTBase serialize(@Nullable T instance);

		@Override
		T deserialize(@Nullable NBTBase nbt);
	}

	interface Contents<T> {

		NBTBase serialize(@Nonnull T instance);

		void deserialize(NBTBase nbt, @Nonnull T instance);

	}

}
