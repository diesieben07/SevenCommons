package de.take_weiland.mods.commons.nbt;

import net.minecraft.nbt.NBTTagCompound;

/**
 * @author diesieben07
 */
public interface NBTSerializable {

	void serialize(NBTTagCompound nbt);

	void deserialize(NBTTagCompound nbt);

}
