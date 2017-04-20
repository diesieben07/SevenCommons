package de.take_weiland.mods.commons.internal.tonbt;

import net.minecraft.nbt.NBTBase;

/**
 * @author diesieben07
 */
public abstract class NBTField {

    final String name;

    public NBTField(String name) {
        this.name = name;
    }

    public abstract void read(NBTBase nbt, Object obj);

    public abstract NBTBase write(Object obj);
}
