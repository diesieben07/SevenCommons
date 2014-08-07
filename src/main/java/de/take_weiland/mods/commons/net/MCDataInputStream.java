package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.util.JavaUtils;

import java.io.InputStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkPositionIndexes;

/**
 * @author diesieben07
 */
public abstract class MCDataInputStream extends InputStream implements MCDataInput {

	static final boolean useUnsafe = JavaUtils.hasUnsafe() && BufferUnsafeChecks.checkUseable();

	public static MCDataInputStream create(byte[] buf) {
		return create(buf, 0, buf.length);
	}

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

	MCDataInputStream() { }

}
