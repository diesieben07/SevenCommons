package de.take_weiland.mods.commons.nbt;

import de.take_weiland.mods.commons.properties.Types;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

/**
 * @author diesieben07
 */
enum GenericSerializer implements NBTSerializer.NullSafe<Object> {

	INSTANCE;

	private static final String TYPE_ID = "_sc$type";
	private static final String DATA_ID = "_sc$data";

	@Override
	public NBTBase serialize(@Nullable Object instance) {
		if (instance == null) {
			return NBT.serializedNull();
		} else {
			NBTTagCompound nbt = new NBTTagCompound();
			String typeID = Types.getID(instance.getClass());

			nbt.setString(TYPE_ID, typeID);
			nbt.setTag(DATA_ID, NBT.getSerializer(typeID).serialize(instance));
			return nbt;
		}
	}

	@Override
	public Object deserialize(@Nullable NBTBase nbt) {
		if (NBT.isSerializedNull(nbt)) {
			return null;
		} else {
			NBTTagCompound compound = (NBTTagCompound) nbt;
			String typeID = compound.getString(TYPE_ID);
			return NBT.getSerializer(typeID).deserialize(compound.getTag(DATA_ID));
		}
	}
}
