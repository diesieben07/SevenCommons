package de.take_weiland.mods.commons.internal.sync.impl;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.ValueSyncer;
import de.take_weiland.mods.commons.util.ItemStacks;
import net.minecraft.item.ItemStack;

/**
 * @author diesieben07
 */
public final class ItemStackSyncer implements ValueSyncer<ItemStack> {

	public ItemStackSyncer() { }

	@Override
	public boolean hasChanged(ItemStack value, Object data) {
		return !ItemStacks.identical((ItemStack) data, value);
	}

	@Override
	public Object writeAndUpdate(ItemStack value, MCDataOutputStream out, Object data) {
		out.writeItemStack(value);
		return ItemStacks.clone(value);
	}

	@Override
	public ItemStack read(MCDataInputStream in, Object data) {
		return in.readItemStack();
	}

	public static final class Contents implements ContentSyncer<ItemStack> {

		public Contents() { }

		@Override
		public boolean hasChanged(ItemStack value, Object data) {
			return !ItemStacks.identical(value, (ItemStack) data);
		}

		@Override
		public Object writeAndUpdate(ItemStack value, MCDataOutputStream out, Object data) {
			out.writeShort(value.itemID);
			out.writeShort(value.getItemDamage());
			out.writeByte(value.stackSize);
			out.writeNBT(value.stackTagCompound);
			return ItemStacks.clone(value);
		}

		@Override
		public void read(ItemStack value, MCDataInputStream in, Object data) {
			value.itemID = in.readShort();
			value.setItemDamage(in.readShort());
			value.stackSize = in.readByte();
			value.stackTagCompound = in.readNBT();
		}
	}
}
