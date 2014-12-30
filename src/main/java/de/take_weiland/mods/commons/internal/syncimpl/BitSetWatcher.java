package de.take_weiland.mods.commons.internal.syncimpl;

import com.google.common.base.Objects;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.SyncableProperty;
import de.take_weiland.mods.commons.sync.Watcher;

import java.util.BitSet;

/**
 * @author diesieben07
 */
public enum BitSetWatcher implements Watcher<BitSet> {
	VALUE {
		@Override
		public void read(MCDataInput in, SyncableProperty<BitSet> property) {
			property.set(in.readBitSet());
		}
	},
	CONTENTS {
		@Override
		public void read(MCDataInput in, SyncableProperty<BitSet> property) {
			in.readBitSet(property.get());
		}
	};

	@Override
	public void setup(SyncableProperty<BitSet> property) {

	}

	@Override
	public void initialWrite(MCDataOutput out, SyncableProperty<BitSet> property) {
		out.writeBitSet(property.get());
	}

	@Override
	public boolean hasChanged(SyncableProperty<BitSet> property) {
		return !Objects.equal(property.get(), property.getData());
	}

	@Override
	public void writeAndUpdate(MCDataOutput out, SyncableProperty<BitSet> property) {
		BitSet val = property.get();
		out.writeBitSet(val);
		property.setData(val == null ? null : val.clone());
	}

}
