package de.take_weiland.mods.commons.net;

import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author diesieben07
 */
public abstract class MCDataOutputStream extends OutputStream implements MCDataOuput {

	private static final int INITIAL_CAP = 64;

	public static MCDataOutputStream create() {
		return create(INITIAL_CAP);
	}

	public static MCDataOutputStream create(int initialCapacity) {
		checkArgument(initialCapacity >= 0, "initialCapacity must be >= 0");
		if (MCDataInputStream.useUnsafe) {
			return new MCDataOutputImplUnsafe(initialCapacity);
		} else {
			return new MCDataOutputImplNonUnsafe(initialCapacity);
		}
	}

	MCDataOutputStream() { }

	// need to override these to satisfy the compiler, because of exceptions
	@Override
	public abstract void write(int b);

	@Override
	public abstract void write(@NotNull byte[] b, int off, int len);

	@Override
	public abstract void write(@NotNull byte[] b);
}
