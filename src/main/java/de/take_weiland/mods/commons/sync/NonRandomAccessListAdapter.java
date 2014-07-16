package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.PacketBuilder;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
* @author diesieben07
*/
class NonRandomAccessListAdapter<V, LIST extends List<V>> extends ListAdapter<V, LIST> {

	NonRandomAccessListAdapter(AdapterCreator<? super V> valueAdapter) {
		super(valueAdapter);
	}

	@Override
	boolean iterativeCheck(LIST list) {
		Iterator<V> it = list.iterator();
		SyncAdapter<? super V>[] adapters = this.adapters;
		boolean changed = false;
		for (int i = 0; it.hasNext(); ++i) {
			changed |= adapters[i].checkAndUpdate(it.next());
		}
		return changed;
	}

	@Override
	public void write(LIST list, PacketBuilder builder) {
		if (list != null) {
			builder.writeVarInt(list.size());
			Iterator<V> it = list.iterator();
			SyncAdapter<? super V>[] adapters = this.adapters;
			for (int i = 0; it.hasNext(); ++i) {
				adapters[i].write(it.next(), builder);
			}
		} else {
			builder.writeVarInt(-1);
		}
	}

	@Override
	public <ACTUAL_LIST extends LIST> ACTUAL_LIST read(ACTUAL_LIST prevVal, DataBuf buf) {
		int len = buf.readVarInt();
		if (len == -1) {
			return null;
		} else {
			SyncAdapter<? super V>[] adapters = this.adapters;
			ListIterator<V> it = prevVal.listIterator();
			int i = 0;
			for (; it.hasNext() && i < len; ++i) {
				it.set(adapters[i].read(it.next(), buf));
			}

			for (; i < len; ++i) {
				it.add(adapters[i].read((V) null, buf));
			}

			if (prevVal.size() != len) { // the list was longer than the new length
				while (it.hasNext()) {
					it.next();
					it.remove();
				}
			}
			return prevVal;
		}
	}
}
