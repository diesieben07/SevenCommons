package de.take_weiland.mods.commons.net;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.IOException;
import java.util.UUID;

/**
 * @author diesieben07
 */
class MCInputImpl implements MCDataInput {

	private final DataInput wrapped;

	MCInputImpl(DataInput wrapped) {
		this.wrapped = wrapped;
	}

	@NotNull
	@Override
	public String readUTF() {
		try {
			return wrapped.readUTF();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void readFully(@NotNull byte[] b) {
		try {
			wrapped.readFully(b);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void readFully(@NotNull byte[] b, int off, int len) {
		try {
			wrapped.readFully(b, off, len);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public int skipBytes(int n) {
		try {
			return wrapped.skipBytes(n);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public boolean readBoolean() {
		try {
			return wrapped.readBoolean();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public byte readByte() {
		try {
			return wrapped.readByte();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public int readUnsignedByte() {
		try {
			return wrapped.readUnsignedByte();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public short readShort() {
		try {
			return wrapped.readShort();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public int readUnsignedShort() {
		try {
			return wrapped.readUnsignedShort();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public char readChar() {
		try {
			return wrapped.readChar();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public int readInt() {
		try {
			return wrapped.readInt();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long readLong() {
		try {
			return wrapped.readLong();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public float readFloat() {
		try {
			return wrapped.readFloat();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public double readDouble() {
		try {
			return wrapped.readDouble();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String readLine() {
		try {
			return wrapped.readLine();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private static final int BYTE_MSB = 0b1000_000;

	@Override
	public int readVarInt() {
		try {
			int res = 0;
			int read;
			int step = 0;
			DataInput wrapped = this.wrapped;
			do {
				read = wrapped.readByte();
				res |= (read & ~BYTE_MSB) << step;
				step += 7;
			} while ((read & BYTE_MSB) == 0);
			return res;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String readString() {
		int len = readVarInt();
		char[] chars = new char[len];
		for (int i = 0; i < len; ++i) {

		}
	}

	@Override
	public ItemStack readItemStack() {
		int id = readShort();
		if (id < 0) {
			return null;
		} else {

		}
	}

	@Override
	public FluidStack readFluidStack() {
		return null;
	}

	@Override
	public UUID readUUID() {
		return null;
	}

	@Override
	public <E extends Enum<E>> E readEnum(Class<E> clazz) {
		return null;
	}

	@Override
	public boolean[] readBooleans() {
		return new boolean[0];
	}

	@Override
	public byte[] readBytes() {
		return new byte[0];
	}

	@Override
	public short[] readShorts() {
		return new short[0];
	}

	@Override
	public int[] readInts() {
		return new int[0];
	}

	@Override
	public long[] readLongs() {
		return new long[0];
	}

	@Override
	public char[] readChars() {
		return new char[0];
	}

	@Override
	public float[] readFloats() {
		return new float[0];
	}

	@Override
	public double[] readDoubles() {
		return new double[0];
	}
}
