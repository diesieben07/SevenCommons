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
public class ItemStackSyncer implements ValueSyncer<ItemStack> {

	private ItemStack companion;

	@Override
	public boolean hasChanged(ItemStack value) {
		return !ItemStacks.identical(companion, value);
	}

	@Override
	public void writeAndUpdate(ItemStack value, MCDataOutputStream out) {
		companion = ItemStacks.clone(value);
		out.writeItemStack(value);
	}

	@Override
	public ItemStack read(MCDataInputStream in) {
		return in.readItemStack();
	}

	public static class Contents implements ContentSyncer<ItemStack> {

		private ItemStack companion;

		@Override
		public boolean hasChanged(ItemStack value) {
			return !ItemStacks.identical(value, companion);
		}

		@Override
		public void writeAndUpdate(ItemStack value, MCDataOutputStream out) {
			companion = ItemStacks.clone(value);
			out.writeItemStack(value);
		}

		@Override
		public void read(ItemStack value, MCDataInputStream in) {
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
