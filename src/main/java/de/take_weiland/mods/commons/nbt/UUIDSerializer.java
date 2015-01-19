package de.take_weiland.mods.commons.nbt;

import de.take_weiland.mods.commons.serialize.NBTSerializer;
import de.take_weiland.mods.commons.serialize.SerializationMethod;
import de.take_weiland.mods.commons.sync.Property;
import net.minecraft.nbt.NBTBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
* @author diesieben07
*/
enum UUIDSerializer implements NBTSerializer<UUID> {

	@NBTSerializer.Provider(forType = UUID.class, method = SerializationMethod.VALUE)
	INSTANCE;

	@Nonnull
	@Override
	public <OBJ> NBTBase serialize(Property<UUID, OBJ> property, OBJ instance) {
		return NBTData.writeUUID(property.get(instance));
	}

	@Override
	public <OBJ> void deserialize(@Nullable NBTBase nbt, Property<UUID, OBJ> property, OBJ instance) {
		property.set(NBTData.readUUID(nbt), instance);
	}


}
