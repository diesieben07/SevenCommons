package de.take_weiland.mods.commons.internal.syncimpl;

import com.google.common.base.Objects;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.SyncableProperty;
import de.take_weiland.mods.commons.sync.Watcher;

/**
 * @author diesieben07
 */
public enum StringWatcher implements Watcher<String> {
	INSTANCE;

	@Override
	public <OBJ> void setup(SyncableProperty<String, OBJ> property, OBJ instance) {

	}

	@Override
	public <OBJ> boolean hasChanged(SyncableProperty<String, OBJ> property, OBJ instance) {
		return !Objects.equal(property.get(instance), property.getData(instance));
	}

	@Override
	public <OBJ> void writeAndUpdate(MCDataOutput out, SyncableProperty<String, OBJ> property, OBJ instance) {
		String val = property.get(instance);
		out.writeString(val);
		property.setData(val, instance);
	}

	@Override
	public <OBJ> void initialWrite(MCDataOutput out, SyncableProperty<String, OBJ> property, OBJ instance) {
		out.writeString(property.get(instance));
	}

	@Override
	public <OBJ> void read(MCDataInput in, SyncableProperty<String, OBJ> property, OBJ instance) {
		property.set(in.readString(), instance);
	}
}
