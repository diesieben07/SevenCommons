package de.take_weiland.mods.commons.internal.syncimpl;

import com.google.common.base.Objects;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.SyncableProperty;
import de.take_weiland.mods.commons.sync.Watcher;

import java.util.UUID;

/**
 * @author diesieben07
 */
public enum UUIDWatcher implements Watcher<UUID> {
	INSTANCE;

	@Override
	public void setup(SyncableProperty<UUID> property) {

	}

	@Override
	public boolean hasChanged(SyncableProperty<UUID> property) {
		return !Objects.equal(property.get(), property.getData());
	}

	@Override
	public void writeAndUpdate(MCDataOutput out, SyncableProperty<UUID> property) {
		UUID val = property.get();
		out.writeUUID(val);
		property.setData(val);
	}

	@Override
	public void initialWrite(MCDataOutput out, SyncableProperty<UUID> property) {
		out.writeUUID(property.get());
	}

	@Override
	public void read(MCDataInput in, SyncableProperty<UUID> property) {
		property.set(in.readUUID());
	}
}
