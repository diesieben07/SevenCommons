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

		@Override
		public boolean hasChanged(ItemStack value, Object data) {
			return !ItemStacks.identical(value, (ItemStack) data);
		}

		@Override
		public Object writeAndUpdate(ItemStack value, MCDataOutputStream out, Object data) {
			out.writeItemStack(value);
			return ItemStacks.clone(value);
		}

		@Override
		public void read(ItemStack value, MCDataInputStream in, Object data) {
			int id = in.readShort();
			if (id == -1) {
				throw new IllegalArgumentException();
			}
			value.itemID = id;
			value.setItemDamage(in.readShort());
			value.stackSize = in.readByte();
			value.stackTagCompound = in.readNBT();
		}
	}
}
