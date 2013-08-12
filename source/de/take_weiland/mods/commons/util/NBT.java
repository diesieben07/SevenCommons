package de.take_weiland.mods.commons.util;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public final class NBT {

	private NBT() { }

	/**
	 * view the given NBTTagList as a {@link List}
	 * the type parameter T can be used if you are sure that this list only contains NBT-Tags of the given type
	 * @param nbtList the list to view
	 * @return a modifiable list view of the NBTTagList
	 */
	public static final <T extends NBTBase> List<T> asList(NBTTagList nbtList) {
		return ((NBTListProxy)nbtList).getWrappedList();
	}

	public static final NBTTagCompound getAttachedNbt(ItemStack stack) {
		if (stack.stackTagCompound == null) {
			stack.stackTagCompound = new NBTTagCompound();
		}
		return stack.stackTagCompound;
	}

	public static final NBTTagCompound getOrCreateCompound(NBTTagCompound parent, String key) {
		if (!parent.hasKey(key)) {
			parent.setCompoundTag(key, parent);
		}
		return parent.getCompoundTag(key);
	}

}
