package de.take_weiland.mods.commons.nbt;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author diesieben07
 */
public interface NBTSerializable {

	NBTBase serialize();

	void deserialize(NBTTagCompound nbt);

}
