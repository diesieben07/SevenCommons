package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.WritableDataBuf;

final class StringSyncer implements TypeSyncer<String> {

	@Override
	public boolean equal(String a, String b) {
		return a.equals(b);
	}

	@Override
	public void write(String instance, WritableDataBuf out) {
		out.writeString(instance);
	}

	@Override
	public String read(String old, DataBuf in) {
		return in.readString();
	}

}
