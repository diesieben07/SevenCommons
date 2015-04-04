package de.take_weiland.mods.commons.internal.tonbt;

import net.minecraft.nbt.NBTTagCompound;

/**
 * @author diesieben07
 */
public abstract class ToNbtHandler {

    public abstract void write(Object object, NBTTagCompound nbt);

    public abstract void read(Object object, NBTTagCompound nbt);

}
