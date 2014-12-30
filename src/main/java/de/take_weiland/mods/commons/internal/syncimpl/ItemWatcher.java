package de.take_weiland.mods.commons.internal.syncimpl;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.SyncableProperty;
import de.take_weiland.mods.commons.sync.Watcher;
import net.minecraft.item.Item;

/**
 * @author diesieben07
 */
public enum ItemWatcher implements Watcher<Item> {
	INSTANCE;

	@Override
	public void setup(SyncableProperty<Item> property) {

	}

	@Override
	public void initialWrite(MCDataOutput out, SyncableProperty<Item> property) {
		out.writeItem(property.get());
	}

	@Override
	public boolean hasChanged(SyncableProperty<Item> property) {
		return property.get() != property.getData();
	}

	@Override
	public void writeAndUpdate(MCDataOutput out, SyncableProperty<Item> property) {
		Item val = property.get();
		out.writeItem(val);
		property.setData(val);
	}

	@Override
	public void read(MCDataInput in, SyncableProperty<Item> property) {
		property.set(in.readItem());
	}
}
