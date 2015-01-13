package de.take_weiland.mods.commons.nbt;

import de.take_weiland.mods.commons.serialize.NBTSerializer;
import de.take_weiland.mods.commons.serialize.SerializationMethod;
import net.minecraft.nbt.NBTBase;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
* @author diesieben07
*/
enum FluidStackSerializer implements NBTSerializer<FluidStack> {

	@NBTSerializer.Provider(forType = FluidStack.class, method = SerializationMethod.VALUE)
	INSTANCE;

	@Override
	public NBTBase serialize(@Nullable FluidStack instance) {
		return NBTData.writeFluidStack(instance);
	}

	@Override
	public FluidStack deserialize(@Nullable NBTBase nbt) {
		return NBTData.readFluidStack(nbt);
	}

	enum Contents implements NBTSerializer.Contents<FluidStack> {

		@NBTSerializer.Provider(forType = FluidStack.class, method = SerializationMethod.CONTENTS)
		INSTANCE;


		@Nonnull
		@Override
		public NBTBase serialize(@Nonnull FluidStack instance) {
			return null;
		}

		@Override
		public void deserialize(@Nonnull FluidStack instance, @Nullable NBTBase nbt) {

		}
	}
}
