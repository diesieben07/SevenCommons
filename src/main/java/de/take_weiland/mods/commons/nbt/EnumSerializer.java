package de.take_weiland.mods.commons.nbt;

import de.take_weiland.mods.commons.SerializationMethod;
import de.take_weiland.mods.commons.serialize.NBTSerializer;
import de.take_weiland.mods.commons.sync.Property;
import net.minecraft.nbt.NBTBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
* @author diesieben07
*/
@SuppressWarnings("rawtypes")
@ParametersAreNonnullByDefault
enum EnumSerializer implements NBTSerializer<Enum> {

	@NBTSerializer.Provider(forType = Enum.class, method = SerializationMethod.Method.VALUE)
	INSTANCE;

	@Nonnull
	@Override
	public <OBJ> NBTBase serialize(Property<Enum, OBJ> property, OBJ instance) {
		return NBTData.writeEnum(property.get(instance));
	}

	@Override
	public <OBJ> void deserialize(@Nullable NBTBase nbt, Property<Enum, OBJ> property, OBJ instance) {
		property.set(NBTData.readEnum(nbt, (Class) property.getRawType()), instance);
	}
}
