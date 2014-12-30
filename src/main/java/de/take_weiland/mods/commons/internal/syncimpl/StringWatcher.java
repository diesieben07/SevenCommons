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
	public void setup(SyncableProperty<String> property) {

	}

	@Override
	public boolean hasChanged(SyncableProperty<String> property) {
		return !Objects.equal(property.get(), property.getData());
	}

	@Override
	public void writeAndUpdate(MCDataOutput out, SyncableProperty<String> property) {
		String val = property.get();
		out.writeString(val);
		property.setData(val);
	}

	@Override
	public void initialWrite(MCDataOutput out, SyncableProperty<String> property) {
		out.writeString(property.get());
	}

	@Override
	public void read(MCDataInput in, SyncableProperty<String> property) {
		property.set(in.readString());
	}
}
