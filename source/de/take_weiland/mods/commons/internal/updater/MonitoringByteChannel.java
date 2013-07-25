package de.take_weiland.mods.commons.internal.updater;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class MonitoringByteChannel implements ReadableByteChannel {

	private final ReadableByteChannel parent;
	private final UpdatableMod mod;
	private final int total;
	private int progress = 0;
	
	public MonitoringByteChannel(ReadableByteChannel parent, UpdatableMod mod, int total) {
		this.parent = parent;
		this.mod = mod;
		this.total = total;
	}

	@Override
	public boolean isOpen() {
		return parent.isOpen();
	}

	@Override
	public void close() throws IOException {
		parent.close();
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		int read = parent.read(dst);
		if (read >= 0) {
			progress += read;
		}
		mod.getController().onUpdateProgress(mod, progress, total);
		return read;
	}

}
