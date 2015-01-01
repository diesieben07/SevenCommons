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
	public <OBJ> void setup(SyncableProperty<Item, OBJ> property, OBJ instance) {

	}

	@Override
	public <OBJ> void initialWrite(MCDataOutput out, SyncableProperty<Item, OBJ> property, OBJ instance) {
		out.writeItem(property.get(instance));
	}

	@Override
	public <OBJ> boolean hasChanged(SyncableProperty<Item, OBJ> property, OBJ instance) {
		return property.get(instance) != property.getData(instance);
	}

	@Override
	public <OBJ> void writeAndUpdate(MCDataOutput out, SyncableProperty<Item, OBJ> property, OBJ instance) {
		Item val = property.get(instance);
		out.writeItem(val);
		property.setData(val, instance);
	}

	@Override
	public <OBJ> void read(MCDataInput in, SyncableProperty<Item, OBJ> property, OBJ instance) {
		property.set(in.readItem(), instance);
	}
}
