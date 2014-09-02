package de.take_weiland.mods.commons.net;

import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * <p>An implementation of the {@link MCDataOutput} interface, that also provides
 * OutputStream functionality.</p>
 * <p>This implementation writes to a byte array.</p>
 * @author diesieben07
 */
public abstract class MCDataOutputStream extends OutputStream implements MCDataOutput {

	/**
	 * <p>Create a new MCDataOutputStream with a default initial capacity.</p>
	 * @return an MCDataOutputStream
	 */
	public static MCDataOutputStream create() {
		return create(FMLPacketHandlerImpl.STREAMS_INITIAL_CAP);
	}

	/**
	 * <p>Create a new MCDataOutputStream with the given initial capacity.</p>
	 * @param initialCapacity the initial capacity for the backing byte array
	 * @return an MCDataOuputStream
	 */
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
