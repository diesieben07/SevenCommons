package de.take_weiland.mods.commons.nbt;

import de.take_weiland.mods.commons.SerializationMethod;
import de.take_weiland.mods.commons.serialize.NBTSerializer;
import de.take_weiland.mods.commons.sync.Property;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
* @author diesieben07
*/
enum ItemStackSerializer implements NBTSerializer<ItemStack> {

	@NBTSerializer.Provider(forType = ItemStack.class, method = SerializationMethod.Method.VALUE)
	INSTANCE;

	@Nonnull
	@Override
	public <OBJ> NBTBase serialize(Property<ItemStack, OBJ> property, OBJ instance) {
		return NBTData.writeItemStack(property.get(instance));
	}

	@Override
	public <OBJ> void deserialize(@Nullable NBTBase nbt, Property<ItemStack, OBJ> property, OBJ instance) {
		property.set(NBTData.readItemStack(nbt), instance);
	}
}
