package de.take_weiland.mods.commons.nbt;

import de.take_weiland.mods.commons.serialize.NBTSerializer;
import de.take_weiland.mods.commons.serialize.SerializationMethod;
import de.take_weiland.mods.commons.sync.Property;
import net.minecraft.nbt.NBTBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
* @author diesieben07
*/
enum StringSerializer implements NBTSerializer<String> {

	@NBTSerializer.Provider(forType = String.class, method = SerializationMethod.VALUE)
	INSTANCE;

	@Nonnull
	@Override
	public <OBJ> NBTBase serialize(Property<String, OBJ> property, OBJ instance) {
		return NBTData.writeString(property.get(instance));
	}

	@Override
	public <OBJ> void deserialize(@Nullable NBTBase nbt, Property<String, OBJ> property, OBJ instance) {
		property.set(NBTData.readString(nbt), instance);
	}


}
