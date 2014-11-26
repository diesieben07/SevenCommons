package de.take_weiland.mods.commons.sync.impl;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.HandleSubclasses;
import de.take_weiland.mods.commons.sync.ValueSyncer;
import net.minecraft.item.Item;

/**
 * @author diesieben07
 */
public final class ItemSyncer implements ValueSyncer<Item>, HandleSubclasses {

	public ItemSyncer() { }

	@Override
	public boolean hasChanged(Item value, Object data) {
		return value != data;
	}

	@Override
	public Object writeAndUpdate(Item value, MCDataOutputStream out, Object data) {
		out.writeItem(value);
		return value;
	}

	@Override
	public Item read(MCDataInputStream in, Object data) {
		return in.readItem();
	}
}
