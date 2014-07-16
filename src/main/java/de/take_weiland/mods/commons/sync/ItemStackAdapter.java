package de.take_weiland.mods.commons.sync;

import com.google.common.base.Objects;
import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.DataBuffers;
import de.take_weiland.mods.commons.net.PacketBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author diesieben07
 */
class ItemStackAdapter extends SyncAdapter<ItemStack> {

	private int id;
	private int meta;
	private int size;
	private NBTTagCompound nbt;

	@Override
	public boolean checkAndUpdate(ItemStack newValue) {
		if (newValue == null) {
			if (id == -1) {
				return false;
			} else {
				id = -1;
				nbt = null; // for gc
				return true;
			}
		} else {
			if (newValue.itemID != id
					|| newValue.getItemDamage() != meta
					|| newValue.stackSize != size
					|| !Objects.equal(newValue.stackTagCompound, nbt)) {
				id = newValue.itemID;
				meta = newValue.getItemDamage();
				size = newValue.stackSize;
				nbt = NBT.copy(newValue.stackTagCompound);
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public void write(ItemStack value, PacketBuilder builder) {
		if (value != null) {
			builder.writeShort(value.itemID);
			builder.writeShort(value.getItemDamage());
			builder.writeByte(value.stackSize);
			DataBuffers.writeNbt(builder, value.stackTagCompound);
		} else {
			builder.writeShort(-1);
		}
	}

	@Override
	public <ACTUAL_T extends ItemStack> ACTUAL_T read(ACTUAL_T prevVal, DataBuf buf) {
		int itemId = buf.readShort();
		if (itemId == -1) {
			return null;
		} else {
			prevVal.itemID = itemId;
			prevVal.setItemDamage(buf.readShort());
			prevVal.stackSize = buf.readByte();
			prevVal.stackTagCompound = DataBuffers.readNbt(buf);
			return prevVal;
		}
	}
}
