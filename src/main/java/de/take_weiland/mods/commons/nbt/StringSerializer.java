package de.take_weiland.mods.commons.nbt;

import net.minecraft.nbt.NBTBase;

import javax.annotation.Nullable;

/**
* @author diesieben07
*/
class StringSerializer implements NBTSerializer.NullSafe<String> {
	@Override
	public NBTBase serialize(@Nullable String instance) {
		return NBTData.writeString(instance);
	}

	@Override
	public String deserialize(@Nullable NBTBase nbt) {
		return NBTData.readString(nbt);
	}
}
