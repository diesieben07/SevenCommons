package de.take_weiland.mods.commons.internal.syncimpl;

import com.google.common.base.Objects;
import de.take_weiland.mods.commons.SerializationMethod;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.SyncableProperty;
import de.take_weiland.mods.commons.sync.Watcher;

import java.util.BitSet;

/**
 * @author diesieben07
 */
public enum BitSetWatcher implements Watcher<BitSet> {

	@Watcher.Provider(forType = BitSet.class, method = SerializationMethod.Method.VALUE)
	VALUE {
		@Override
		public <OBJ> void read(MCDataInput in, SyncableProperty<BitSet, OBJ> property, OBJ instance) {
			property.set(in.readBitSet(), instance);
		}
	},

	@Watcher.Provider(forType = BitSet.class, method = SerializationMethod.Method.CONTENTS)
	CONTENTS {
		@Override
		public <OBJ> void read(MCDataInput in, SyncableProperty<BitSet, OBJ> property, OBJ instance) {
			in.readBitSet(property.get(instance));
		}
	};

	@Override
	public <OBJ> void setup(SyncableProperty<BitSet, OBJ> property, OBJ instance) {

	}

	@Override
	public <OBJ> void initialWrite(MCDataOutput out, SyncableProperty<BitSet, OBJ> property, OBJ instance) {
		out.writeBitSet(property.get(instance));
	}

	@Override
	public <OBJ> boolean hasChanged(SyncableProperty<BitSet, OBJ> property, OBJ instance) {
		return !Objects.equal(property.get(instance), property.getData(instance));
	}

	@Override
	public <OBJ> void writeAndUpdate(MCDataOutput out, SyncableProperty<BitSet, OBJ> property, OBJ instance) {
		BitSet val = property.get(instance);
		out.writeBitSet(val);
		property.setData(val == null ? null : val.clone(), instance);
	}

}
