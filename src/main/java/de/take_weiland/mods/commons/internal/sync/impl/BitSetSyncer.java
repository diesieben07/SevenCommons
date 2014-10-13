package de.take_weiland.mods.commons.internal.sync.impl;

import com.google.common.base.Objects;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.ContainerSyncer;
import de.take_weiland.mods.commons.sync.PropertySyncer;

import java.util.BitSet;

/**
 * @author diesieben07
 */
public class BitSetSyncer implements PropertySyncer<BitSet> {

	private BitSet companion;

	@Override
	public boolean hasChanged(BitSet value) {
		return !Objects.equal(companion, value);
	}

	@Override
	public void writeAndUpdate(BitSet value, MCDataOutputStream out) {
		if (value == null) {
			companion = null;
		} else if (companion == null) {
			companion = (BitSet) value.clone();
		} else {
			companion.clear();
			companion.or(value);
		}
		out.writeBitSet(value);
	}

	@Override
	public BitSet read(MCDataInputStream in) {
		return in.readBitSet();
	}

	public static class Contents implements ContainerSyncer<BitSet> {

		private BitSet companion;

		@Override
		public boolean hasChanged(BitSet value) {
			return !Objects.equal(companion, value);
		}

		@Override
		public void writeAndUpdate(BitSet value, MCDataOutputStream out) {
			if (companion == null) {
				companion = (BitSet) value.clone();
			} else {
				companion.clear();
				companion.or(value);
			}
		}

		@Override
		public void read(BitSet value, MCDataInputStream in) {
			in.readBitSet(value);
		}
	}
}
