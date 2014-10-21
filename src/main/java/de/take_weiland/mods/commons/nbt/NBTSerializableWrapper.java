package de.take_weiland.mods.commons.nbt;

import de.take_weiland.mods.commons.internal.StaticDeserializerCaller;
import net.minecraft.nbt.NBTBase;

import javax.annotation.Nullable;

/**
* @author diesieben07
*/
class NBTSerializableWrapper<T extends NBTSerializable> implements NBTSerializer.NullSafe<T> {

	private final Class<T> clazz;

	public NBTSerializableWrapper(Class<T> clazz) {
		this.clazz = clazz;
	}

	@Override
	public NBTBase serialize(@Nullable T instance) {
		return instance == null ? NBT.serializedNull() : instance.serialize();
	}

	@Override
	public T deserialize(@Nullable NBTBase nbt) {
		return NBT.isSerializedNull(nbt)
				? null
				: StaticDeserializerCaller.readNBTViaDeserializer(clazz, nbt);
	}
}
