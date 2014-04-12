package de.take_weiland.mods.commons.internal.updater;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class MonitoringByteChannel implements ReadableByteChannel {

	private final ReadableByteChannel parent;
	private final UpdateControllerLocal controller;

	public MonitoringByteChannel(ReadableByteChannel parent, UpdateControllerLocal controller) {
		this.parent = parent;
		this.controller = controller;
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
			controller.onBytesDownloaded(read);
		}
		return read;
	}

}
