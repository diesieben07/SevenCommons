package de.take_weiland.mods.commons.nbt;

import net.minecraft.nbt.NBTTagCompound;

/**
 * @author diesieben07
 */
public interface NBTSerializer<T> {

	NBTTagCompound serialize(T instance);

	T deserialize(NBTTagCompound nbt);


}
