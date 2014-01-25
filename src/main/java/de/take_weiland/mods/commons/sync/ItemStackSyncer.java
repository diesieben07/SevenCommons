package de.take_weiland.mods.commons.sync;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import de.take_weiland.mods.commons.util.ItemStacks;

final class ItemStackSyncer implements TypeSyncer<ItemStack> {

	@Override
	public boolean equal(ItemStack now, ItemStack prev) {
		return ItemStacks.equal(now, prev);
	}

	@Override
	public void write(ItemStack instance, DataOutput out) throws IOException {
		Packet.writeItemStack(instance, out);
	}

	@Override
	public ItemStack read(ItemStack old, DataInput in) throws IOException {
		return Packet.readItemStack(in);
	}

}
