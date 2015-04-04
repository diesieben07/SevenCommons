package de.take_weiland.mods.commons.nbt;

import net.minecraft.nbt.NBTBase;

/**
 * @author diesieben07
 */
public interface NBTSerializer<T> {

    NBTBase write(T value);

    T read(T value, NBTBase nbt);

}
