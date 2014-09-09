package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.nbt.NBT;
import net.minecraft.nbt.*;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * @author diesieben07
 */
public final class NBTSerialization {

	private static final byte NULL = -1;
	private static final String NULL_KEY = "_sc$null";

	public static final String SERIALIZED_NULL = "serializedNull";
	public static final String IS_SERIALIZED_NULL = "isSerializedNull";

	public static NBTBase serializedNull() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setByte(NULL_KEY, NULL);
		return nbt;
	}

	public static boolean isSerializedNull(NBTBase nbt) {
		return nbt.getId() == NBT.TAG_COMPOUND && ((NBTTagCompound) nbt).getByte(NULL_KEY) == NULL;
	}

	public static NBTBase writeUUID(@NotNull UUID uuid) {
		NBTTagList nbt = new NBTTagList();
		nbt.appendTag(new NBTTagLong("", uuid.getMostSignificantBits()));
		nbt.appendTag(new NBTTagLong("", uuid.getLeastSignificantBits()));
		return nbt;
	}

	public static UUID readUUID(NBTBase nbt) {
		if (nbt.getId() != NBT.TAG_LIST) {
			return null;
		} else {
			NBTTagList list = (NBTTagList) nbt;
			return new UUID(((NBTTagLong) list.tagAt(0)).data, ((NBTTagLong) list.tagAt(1)).data);
		}
	}

	private NBTSerialization() { }

}
