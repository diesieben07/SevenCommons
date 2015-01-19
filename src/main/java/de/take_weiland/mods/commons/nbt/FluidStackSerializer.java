package de.take_weiland.mods.commons.nbt;

import de.take_weiland.mods.commons.serialize.NBTSerializer;
import de.take_weiland.mods.commons.serialize.SerializationMethod;
import de.take_weiland.mods.commons.sync.Property;
import net.minecraft.nbt.NBTBase;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
* @author diesieben07
*/
enum FluidStackSerializer implements NBTSerializer<FluidStack> {

	@NBTSerializer.Provider(forType = FluidStack.class, method = SerializationMethod.VALUE)
	VALUE;

	@Nonnull
	@Override
	public <OBJ> NBTBase serialize(Property<FluidStack, OBJ> property, OBJ instance) {
		return NBTData.writeFluidStack(property.get(instance));
	}

	@Override
	public <OBJ> void deserialize(@Nullable NBTBase nbt, Property<FluidStack, OBJ> property, OBJ instance) {
		property.set(NBTData.readFluidStack(nbt), instance);
	}
}
