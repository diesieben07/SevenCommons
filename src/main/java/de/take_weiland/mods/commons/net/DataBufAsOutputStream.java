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
		buf.putByte((byte) b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		buf.putRaw(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		buf.putRaw(b, off, len);
	}

}
