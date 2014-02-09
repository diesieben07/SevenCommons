package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.Packets;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import de.take_weiland.mods.commons.util.ItemStacks;
import net.minecraft.item.ItemStack;

final class ItemStackSyncer implements TypeSyncer<ItemStack> {

	@Override
	public boolean equal(ItemStack now, ItemStack prev) {
		return ItemStacks.equal(now, prev);
	}

	@Override
	public void write(ItemStack instance, WritableDataBuf out) {
		Packets.writeItemStack(out, instance);
	}

	@Override
	public ItemStack read(ItemStack old, DataBuf in) {
		return Packets.readItemStack(in);
	}

}
