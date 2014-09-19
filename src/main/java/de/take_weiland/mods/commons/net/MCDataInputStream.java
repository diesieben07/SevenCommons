package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.util.JavaUtils;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

import static com.google.common.base.Preconditions.*;

/**
 * <p>An implementation of the {@link de.take_weiland.mods.commons.net.MCDataInput} interface that also provides
 * InputStream functionality.</p>
 * <p>This InputStream does not throw IOExceptions. The {@link #mark(int)} functionality is supported.</p>
 * @author diesieben07
 */
public abstract class MCDataInputStream extends InputStream implements MCDataInput {

	static final boolean useUnsafe = JavaUtils.hasUnsafe() && BufferUtils.canUseUnsafe();

	/**
	 * <p>Create a new MCDataInputStream that reads from the given byte array.</p>
	 * @param buf the array to read from
	 * @return an MCDataInputStream
	 */
	public static MCDataInputStream create(byte[] buf) {
		return create(buf, 0, buf.length);
	}

	/**
	 * <p>Create a new MCDataInputStream that reads at most {@code len} bytes from the given byte array, starting at position
	 * {@code pos}.</p>
	 * @param buf the array to read from
	 * @param off the starting position
	 * @param len the number of bytes to read
	 * @return an MCDataInputStream
	 */
	public static MCDataInputStream create(byte[] buf, int off, int len) {
		checkArgument(len >= 0, "len must be >= 0");
		checkArgument(off >= 0, "off must be >= 0");
		checkPositionIndexes(off, off + len, checkNotNull(buf, "buf").length);
		if (useUnsafe) {
			return new MCDataInputImplUnsafe(buf, off, len);
		} else {
			return new MCDataInputImplNonUnsafe(buf, off, len);
		}
	}

	@Override
	public abstract int read();

	@Override
	public abstract int read(@NotNull byte[] b);

	@Override
	public abstract int read(@NotNull byte[] b, int off, int len);

	@Override
	public abstract long skip(long n);

	@Override
	public abstract int available();

	@Override
	public abstract void close();

	@Override
	public abstract void mark(int readlimit);

	@Override
	public abstract void reset();

	@Override
	public final boolean markSupported() {
		return true;
	}

	MCDataInputStream() { }

}
