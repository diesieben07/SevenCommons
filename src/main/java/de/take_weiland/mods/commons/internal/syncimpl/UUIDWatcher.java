package de.take_weiland.mods.commons.internal.syncimpl;

import com.google.common.base.Objects;
import de.take_weiland.mods.commons.SerializationMethod;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.SyncableProperty;
import de.take_weiland.mods.commons.sync.Watcher;

import java.util.UUID;

/**
 * @author diesieben07
 */
public enum UUIDWatcher implements Watcher<UUID> {

	@Watcher.Provider(forType = UUID.class, method = SerializationMethod.Method.VALUE)
	INSTANCE;

	@Override
	public <OBJ> void setup(SyncableProperty<UUID, OBJ> property, OBJ instance) {

	}

	@Override
	public <OBJ> boolean hasChanged(SyncableProperty<UUID, OBJ> property, OBJ instance) {
		return !Objects.equal(property.get(instance), property.getData(instance));
	}

	@Override
	public <OBJ> void writeAndUpdate(MCDataOutput out, SyncableProperty<UUID, OBJ> property, OBJ instance) {
		UUID val = property.get(instance);
		out.writeUUID(val);
		property.setData(val, instance);
	}

	@Override
	public <OBJ> void initialWrite(MCDataOutput out, SyncableProperty<UUID, OBJ> property, OBJ instance) {
		out.writeUUID(property.get(instance));
	}

	@Override
	public <OBJ> void read(MCDataInput in, SyncableProperty<UUID, OBJ> property, OBJ instance) {
		property.set(in.readUUID(), instance);
	}
}
