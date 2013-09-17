package de.take_weiland.mods.commons.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import com.google.common.io.ByteArrayDataInput;

public interface MinecraftDataInput extends ByteArrayDataInput {

	ItemStack readItemStack();
	
	FluidStack readFluidStack();
	
	NBTTagCompound readNBTTagCompound();
	
	<E extends Enum<E>> E readEnum(Class<E> clazz);
	
}
