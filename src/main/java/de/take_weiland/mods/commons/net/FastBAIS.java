package de.take_weiland.mods.commons.net;

import com.google.common.primitives.UnsignedBytes;
import de.take_weiland.mods.commons.util.JavaUtils;
import de.take_weiland.mods.commons.util.SCReflector;
import de.take_weiland.mods.commons.util.UnsignedShorts;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkPositionIndexes;

/**
 * @author diesieben07
 */
abstract class FastBAIS extends InputStream implements MCDataInput {

	final byte[] buf;
	private final int maxLen;
	int pos;

	static FastBAIS create(byte[] buf) {
		return create(buf, 0, buf.length);
	}

	static FastBAIS create(byte[] buf, int off, int len) {
		checkPositionIndexes(off, off + len, buf.length);
		if (useUnsafe) {
			return new FastBAISUnsafe(buf, off, len);
		} else {
			return new FastBAISNonUnsafe(buf, off, len);
		}
	}

	private static final boolean useUnsafe = JavaUtils.hasUnsafe() && UnsafeCheck.checkUseable();

	private static class UnsafeCheck {

		static boolean checkUseable() {
			Unsafe unsafe = (Unsafe) JavaUtils.getUnsafe();
			// sanity checks to see if the native memory layout allows us to use the fast array copying
			if (ByteOrder.nativeOrder() != ByteOrder.LITTLE_ENDIAN
				|| unsafe.arrayIndexScale(byte[].class) != 1
				|| unsafe.arrayIndexScale(short[].class) != 2
				|| unsafe.arrayIndexScale(int[].class) != 4
				|| unsafe.arrayIndexScale(long[].class) != 8
				|| unsafe.arrayIndexScale(float[].class) != 4
				|| unsafe.arrayIndexScale(double[].class) != 8) {
				return false;
			}

			long bits = Double.doubleToRawLongBits(3.4);
			double[] d = new double[1];
			unsafe.putLong(d, (long) unsafe.arrayBaseOffset(double[].class), bits);
			if (d[0] != 3.4) {
				return false;
			}

			int bits2 = Float.floatToRawIntBits(3.4f);
			float[] f = new float[1];
			unsafe.putInt(f, (long) unsafe.arrayBaseOffset(float[].class), bits2);
			return f[0] == 3.4f;
		}
	}

	FastBAIS(byte[] buf, int off, int len) {
		this.buf = buf;
		this.pos = off;
		this.maxLen = len;
	}

	@Override
	public int read() {
		return pos < maxLen ? buf[pos++] : -1;
	}

	@Override
	public int read(@NotNull byte[] b, int off, int len) {
		checkPositionIndexes(off, off + len, buf.length);
		int avail = maxLen - pos;
		if (avail == 0) {
			return -1;
		}
		int actualLen = Math.min(len, avail);
		if (actualLen != 0) {
			System.arraycopy(buf, pos, b, off, actualLen);
			pos += actualLen;
		}
		return actualLen;
	}

	@Override
	public int read(@NotNull byte[] b) {
		return read(b, 0, b.length);
	}

	final void checkAvailable(int bytes) {
		if (maxLen - pos < bytes) {
			throw new IllegalStateException("Read past end of buffer");
		}
	}

	@NotNull
	@Override
	public String readUTF() {
		try {
			return DataInputStream.readUTF(this);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	@Deprecated
	public String readLine() {
		StringBuilder sb = new StringBuilder();

		int c;

		loop:
		while (true) {
			switch (c = read()) {
				case -1:
				case '\n':
					break loop;
				case '\r':
					// \r\n counts as one line break
					if (pos != maxLen && buf[pos] == '\n') {
						pos++;
					}
					break loop;
				default:
					sb.append((char) c);
					break;
			}
		}

		return c == -1 && sb.length() == 0 ? null : sb.toString();
	}

	@Override
	public double readDouble() {
		return Double.longBitsToDouble(readLong());
	}

	@Override
	public float readFloat() {
		return Float.intBitsToFloat(readInt());
	}

	@Override
	public long readLong() {
		checkAvailable(8);
		byte[] buf = this.buf;
		int pos = this.pos;
		this.pos = pos + 8;
		return (long) buf[pos] & 0xFF
				| (long) (buf[pos + 1] & 0xFF) << 8
				| (long) (buf[pos + 2] & 0xFF) << 16
				| (long) (buf[pos + 3] & 0xFF) << 24
				| (long) (buf[pos + 4] & 0xFF) << 32
				| (long) (buf[pos + 5] & 0xFF) << 40
				| (long) (buf[pos + 6] & 0xFF) << 48
				| (long) (buf[pos + 7] & 0xFF) << 56;

	}

	@Override
	public int readInt() {
		checkAvailable(4);
		byte[] buf = this.buf;
		int pos = this.pos;
		this.pos = pos + 4;
		return (buf[pos] & 0xFF)
				| (buf[pos + 1] & 0xFF) << 8
				| (buf[pos + 2] & 0xFF) << 16
				| (buf[pos + 3] & 0xFF) << 24;
	}

	@Override
	public char readChar() {
		checkAvailable(2);
		byte[] buf = this.buf;
		int pos = this.pos;
		this.pos = pos + 2;
		return (char) ((buf[pos] & 0xFF) | (buf[pos + 1] & 0xFF) << 8);
	}

	@Override
	public int readUnsignedShort() {
		return UnsignedShorts.toInt(readShort());
	}

	@Override
	public short readShort() {
		checkAvailable(2);
		byte[] buf = this.buf;
		int pos = this.pos;
		this.pos = pos + 2;
		return (short) (buf[pos] & 0xFF | buf[pos + 1] << 8);
	}

	@Override
	public int readUnsignedByte() {
		return UnsignedBytes.toInt(readByte());
	}

	@Override
	public byte readByte() {
		checkAvailable(1);
		return buf[pos++];
	}

	@Override
	public boolean readBoolean() {
		return readByte() == 1;
	}

	@Override
	public int skipBytes(int n) {
		int avail = maxLen - pos;
		if (n > avail) n = avail;
		pos += n;
		return n;
	}

	@Override
	public void readFully(@NotNull byte[] b, int off, int len) {
		checkPositionIndexes(off, off + len, b.length);
		checkAvailable(len);
		System.arraycopy(buf, pos, b, off, len);
		pos += len;
	}

	@Override
	public void readFully(@NotNull byte[] b) {
		readFully(b, 0, b.length);
	}

	@Override
	public <E extends Enum<E>> E readEnum(Class<E> clazz) {
		int e = readVarInt();
		return e < 0 ? null : JavaUtils.byOrdinal(clazz, e);
	}

	private static final int BYTE_MSB = 0b1000_0000;
	private static final int SEVEN_BITS = 0b0111_1111;

	@Override
	public int readVarInt() {
		int res = 0;
		int read;
		int step = 0;

		do {
			read = readByte();
			res |= (read & SEVEN_BITS) << step;
			step += 7;
		} while ((read & BYTE_MSB) == 0);
		return res;
	}

	@Override
	public String readString() {
		int len = readVarInt();
		if (len < 0) {
			return null;
		} else if (len == 0) {
			return "";
		} else {
			checkAvailable(len << 1);
			byte[] buf = this.buf;
			int pos = this.pos;
			char[] chars = new char[len];
			for (int i = 0; i < len; ++i) {
				chars[i] = (char) (buf[pos] | buf[pos + 1] << 8);
				pos += 2;
			}
			this.pos = pos;
			return SCReflector.instance.createStringShared(chars, true);
		}
	}

	@Override
	public ItemStack readItemStack() {
		int id = readShort();
		if (id < 0) {
			return null;
		} else {
			int dmg = readShort();
			int size = readByte();
			ItemStack stack = new ItemStack(id, size, dmg);
			stack.stackTagCompound = readNbt();
			return stack;
		}
	}

	@Override
	public FluidStack readFluidStack() {
		int id = readVarInt();
		if (id < 0) {
			return null;
		} else {
			return new FluidStack(id, readVarInt(), readNbt());
		}
	}

	@Override
	public UUID readUUID() {
		return new UUID(readLong(), readLong());
	}

	@Override
	public NBTTagCompound readNbt() {
		int isNull = readByte();
		if (isNull == 0) {
			return null;
		} else {
			NBTTagCompound nbt = new NBTTagCompound();
			try {
				SCReflector.instance.load(nbt, this, 0);
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
			return nbt;
		}
	}

	@Override
	public boolean[] readBooleans(boolean[] b) {
		int len = readVarInt();
		if (len < 0) {
			return null;
		} else if (len == 0) {
			return ArrayUtils.nullToEmpty(b);
		} else {
			if (b == null || b.length < len) {
				b = new boolean[len];
			}
			int byteLen = (len - 1) / 8 + 1; // works for len > 0
			checkAvailable(byteLen);

			byte[] buf = this.buf;
			int pos = this.pos;
			this.pos = pos + byteLen;

			int read = 0;
			int mask = 0;
			for (int idx = 0; idx < len; ++idx) {
				if (idx % 8 == 0) {
					read = buf[pos++] & 0xFF;
					mask = 0b0000_0001;
				}
				b[idx] = (read & mask) != 0;
				mask <<= 1;
			}

			return b;
		}
	}

	@Override
	public byte[] readBytes(byte[] b) {
		int len = readVarInt();
		if (len < 0) {
			return null;
		} else if (len == 0) {
			return ArrayUtils.nullToEmpty(b);
		} else {
			if (b == null || b.length < len) {
				b = new byte[len];
			}
			checkAvailable(len);
			System.arraycopy(buf, pos, b, 0, len);
			pos += len;
			return b;
		}
	}

	@Override
	public boolean[] readBooleans() {
		return readBooleans(null);
	}

	@Override
	public byte[] readBytes() {
		return readBytes(null);
	}

	@Override
	public short[] readShorts() {
		return readShorts(null);
	}

	@Override
	public int[] readInts() {
		return readInts(null);
	}

	@Override
	public long[] readLongs() {
		return readLongs(null);
	}

	@Override
	public char[] readChars() {
		return readChars(null);
	}

	@Override
	public float[] readFloats() {
		return readFloats(null);
	}

	@Override
	public double[] readDoubles() {
		return readDoubles(null);
	}
}
