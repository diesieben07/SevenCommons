package de.take_weiland.mods.commons.nbt;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;

import javax.annotation.Nullable;

/**
* @author diesieben07
*/
class ItemStackSerializer implements NBTSerializer.NullSafe<ItemStack> {
	@Override
	public NBTBase serialize(@Nullable ItemStack instance) {
		return NBTData.writeItemStack(instance);
	}

	@Override
	public ItemStack deserialize(@Nullable NBTBase nbt) {
		return NBTData.readItemStack(nbt);
	}
}
