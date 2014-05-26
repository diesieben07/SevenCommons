package de.take_weiland.mods.commons.net;

import com.google.common.primitives.Ints;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

final class DataBufAsInputstream extends InputStream {

	private final DataBuf buf;

	DataBufAsInputstream(DataBuf buf) {
		this.buf = buf;
	}

	@Override
	public int read() throws IOException {
		return buf.readByte() & 0xFF;
	}

	@Override
	public int read(@NotNull byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(@NotNull byte[] b, int off, int len) throws IOException {
        return buf.copyTo(b, off, len);
	}

	@Override
	public int available() throws IOException {
		return buf.available();
	}

	@Override
	public long skip(long n) throws IOException {
		if (n <= 0) {
			return 0;
		}
		int toSkip = Math.max(Ints.saturatedCast(n), buf.available());
		buf.seek(buf.pos() + toSkip);
		return toSkip;
	}

}