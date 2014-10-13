package de.take_weiland.mods.commons.nbt;

import net.minecraft.nbt.NBTBase;

import javax.annotation.Nullable;
import java.util.UUID;

/**
* @author diesieben07
*/
class UUIDSerializer implements NBTSerializer.NullSafe<UUID> {
	@Override
	public NBTBase serialize(@Nullable UUID instance) {
		return NBT.writeUUID(instance);
	}

	@Override
	public UUID deserialize(@Nullable NBTBase nbt) {
		return NBT.readUUID(nbt);
	}
}
