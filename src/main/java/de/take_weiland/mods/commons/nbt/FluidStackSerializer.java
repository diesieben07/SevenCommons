package de.take_weiland.mods.commons.nbt;

import net.minecraft.nbt.NBTBase;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

/**
* @author diesieben07
*/
class FluidStackSerializer implements NBTSerializer.NullSafe<FluidStack> {

	@Override
	public NBTBase serialize(@Nullable FluidStack instance) {
		return NBT.writeFluidStack(instance);
	}

	@Override
	public FluidStack deserialize(@Nullable NBTBase nbt) {
		return NBT.readFluidStack(nbt);
	}
}
