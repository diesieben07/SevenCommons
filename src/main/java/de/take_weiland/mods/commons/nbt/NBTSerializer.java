package de.take_weiland.mods.commons.nbt;

import net.minecraft.nbt.NBTTagCompound;

/**
 * @author diesieben07
 */
public interface NBTSerializer<T> {

	void serialize(T instance, NBTTagCompound nbt);

	T deserialize(NBTTagCompound nbt);


}
