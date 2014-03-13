package de.take_weiland.mods.commons.metadata;

import net.minecraft.nbt.NBTTagCompound;

/**
 * @author diesieben07
 */
public interface Metadata {

	interface Extended {

		void write(NBTTagCompound nbt);

		void read(NBTTagCompound nbt);

	}

}
