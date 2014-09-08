package de.take_weiland.mods.commons.nbt;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author diesieben07
 */
public interface NBTSerializer<T> {

	net.minecraft.nbt.NBTBase serialize(T instance);

	T deserialize(NBTBase nbt);


}
