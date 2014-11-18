package de.take_weiland.mods.commons.internal.sync.impl;

import com.google.common.base.Objects;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.ValueSyncer;

import java.util.BitSet;

/**
 * @author diesieben07
 */
public final class BitSetSyncer implements ValueSyncer<BitSet> {

	@Override
	public boolean hasChanged(BitSet value, Object data) {
		return !Objects.equal(data, value);
	}

	@Override
	public Object writeAndUpdate(BitSet value, MCDataOutputStream out, Object data) {
		if (value == null) {
			data = null;
		} else if (data == null) {
			data = value.clone();
		} else {
			BitSet bs = (BitSet) data;
			bs.clear();
			bs.or(value);
		}
		out.writeBitSet(value);
		return data;
	}

	@Override
	public BitSet read(MCDataInputStream in, Object data) {
		return in.readBitSet();
	}

	public static final class Contents implements ContentSyncer<BitSet> {

		@Override
		public boolean hasChanged(BitSet value, Object data) {
			return !Objects.equal(data, value);
		}

		@Override
		public Object writeAndUpdate(BitSet value, MCDataOutputStream out, Object data) {
			if (data == null) {
				data = value.clone();
			} else {
				BitSet bs = (BitSet) data;
				bs.clear();
				bs.or(value);
			}
			return data;
		}

		@Override
		public void read(BitSet value, MCDataInputStream in, Object data) {
			in.readBitSet(value);
		}
	}
}
