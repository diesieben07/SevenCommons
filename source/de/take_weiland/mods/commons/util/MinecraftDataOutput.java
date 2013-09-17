package de.take_weiland.mods.commons.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import com.google.common.io.ByteArrayDataOutput;

public interface MinecraftDataOutput extends ByteArrayDataOutput {

	void writeItemStack(ItemStack stack);
	
	void writeFluidStack(FluidStack stack);
	
	void writeNBTTagCompound(NBTTagCompound nbt);
	
	void writeEnum(Enum<?> e);
	
}
