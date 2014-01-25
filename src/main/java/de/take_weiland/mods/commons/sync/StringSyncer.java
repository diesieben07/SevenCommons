package de.take_weiland.mods.commons.sync;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

final class StringSyncer implements TypeSyncer<String> {

	@Override
	public boolean equal(String a, String b) {
		return a.equals(b);
	}

	@Override
	public void write(String instance, DataOutput out) throws IOException {
		out.writeUTF(instance);
	}

	@Override
	public String read(DataInput in) throws IOException {
		return in.readUTF();
	}

}
