package de.take_weiland.mods.commons.net;

import java.io.IOException;
import java.io.InputStream;

import com.google.common.primitives.Ints;

final class DataBufAsInputstream extends InputStream {

	private final DataBuf buf;

	DataBufAsInputstream(DataBuf buf) {
		this.buf = buf;
	}

	@Override
	public int read() throws IOException {
		return buf.getByte() & 0xFF;
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
        return buf.copyTo(b, off, len);
	}

	@Override
	public int available() throws IOException {
		return buf.available();
	}

	@Override
	public long skip(long n) throws IOException {
		int toSkip = Math.max(Ints.saturatedCast(n), buf.available());
		buf.seek(buf.pos() + toSkip);
		return toSkip;
	}

}