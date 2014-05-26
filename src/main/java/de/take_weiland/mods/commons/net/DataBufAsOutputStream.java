package de.take_weiland.mods.commons.net;

import java.io.IOException;
import java.io.OutputStream;

final class DataBufAsOutputStream extends OutputStream {

	private final WritableDataBuf buf;

	public DataBufAsOutputStream(WritableDataBuf buf) {
		this.buf = buf;
	}

	@Override
	public void write(int b) throws IOException {
		buf.writeByte((byte) b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		buf.writeRaw(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		buf.writeRaw(b, off, len);
	}

}
