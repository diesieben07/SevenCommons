package de.take_weiland.mods.commons.internal.sync.impl;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.ValueSyncer;
import net.minecraft.item.Item;

/**
 * @author diesieben07
 */
public class ItemSyncer implements ValueSyncer<Item> {

	private Item companion;

	@Override
	public boolean hasChanged(Item value) {
		return value != companion;
	}

	@Override
	public void writeAndUpdate(Item value, MCDataOutputStream out) {
		companion = value;
		out.writeItem(value);
	}

	@Override
	public Item read(MCDataInputStream in) {
		return in.readItem();
	}
}
