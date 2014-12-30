package de.take_weiland.mods.commons.serialize;

import net.minecraft.nbt.NBTBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author diesieben07
 */
public interface NBTSerializer<T> {

	@Nonnull
	NBTBase serialize(@Nullable T instance);

	@Nullable
	T deserialize(@Nullable NBTBase nbt);

	interface Contents<T> {

		@Nonnull
		NBTBase serialize(@Nonnull T instance);

		void deserialize(@Nullable NBTBase nbt, @Nonnull T instance);

	}

}
