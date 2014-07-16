package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.PacketBuilder;

import java.util.List;

/**
* @author diesieben07
*/ // specialized for RandomAccess lists to avoid iterators
class RandomAccessListAdapter<V, LIST extends List<V>> extends ListAdapter<V, LIST> {

	RandomAccessListAdapter(AdapterCreator<? super V> valueAdapter) {
		super(valueAdapter);
	}

	@Override
	boolean iterativeCheck(LIST list) {
		SyncAdapter<? super V>[] adapters = this.adapters;
		boolean changed = false;
		int len = list.size();
		for (int i = 0; i < len; ++i) {
			changed |= adapters[i].checkAndUpdate(list.get(i));
		}
		return changed;
	}

	@Override
	public void write(LIST value, PacketBuilder builder) {
		if (value == null) {
			builder.writeVarInt(-1);
		} else {
			int len;
			builder.writeVarInt((len = value.size()));
			SyncAdapter<? super V>[] adapters = this.adapters;
			for (int i = 0; i < len; ++i) {
				adapters[i].write(value.get(i), builder);
			}
		}
	}

	@Override
	public <ACTUAL_LIST extends LIST> ACTUAL_LIST read(ACTUAL_LIST prevVal, DataBuf buf) {
		int len = buf.readVarInt();
		if (len == -1) {
			return null;
		} else {
			int prevSize = prevVal.size();
			if (prevSize > len) {
				prevVal.subList(len, prevSize).clear();
			}
			SyncAdapter<? super V>[] adapters = this.adapters;
			int i = 0;
			for (; i < len && i < prevSize; ++i) {
				prevVal.set(i, adapters[i].read(prevVal.get(i), buf));
			}

			// add any elements that were "longer" than the current list
			for (; i < len; ++i) {
				prevVal.add(adapters[i].read((V) null, buf));
			}

			return prevVal;
		}
	}

}
