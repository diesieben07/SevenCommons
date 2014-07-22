package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.util.SCReflector;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author diesieben07
 */
class FastBAOS extends OutputStream implements MCDataOuput {

	private byte[] buf;
	private int count;
	private OutputStream compressor;

	FastBAOS() {
		this(64);
	}

	FastBAOS(int initialCap) {
		buf = new byte[initialCap];
	}

	private void ensureCapacity(int bytesToWrite) {
		if ((count + bytesToWrite) - buf.length > 0) {
			grow(bytesToWrite + count);
		}
	}

	private void grow(int minCapacity) {
		int oldCapacity = buf.length;
		int newCapacity = oldCapacity << 1;
		if (newCapacity - minCapacity < 0)
			newCapacity = minCapacity;
		if (newCapacity < 0) {
			if (minCapacity < 0)
				throw new OutOfMemoryError();
			newCapacity = Integer.MAX_VALUE;
		}
		buf = Arrays.copyOf(buf, newCapacity);
	}

	@Override
	public void write(int b) {
		ensureCapacity(1);
		buf[count++] = (byte) b;
	}

	@Override
	public void write(@NotNull byte[] b) {
		write(b, 0, b.length);
	}

	@Override
	public void write(@NotNull byte[] b, int off, int len) {
		ensureCapacity(len);
		System.arraycopy(b, off, buf, count, len);
		count += len;
	}

	@Override
	public void flush() { }

	@Override
	public void close() { }

	@Override
	public void writeBoolean(boolean v) {
		write(v ? 1 : 0);
	}

	@Override
	public void writeByte(int v) {
		write(v);
	}

	@Override
	public void writeShort(int v) {
		ensureCapacity(2);
		buf[count++] = (byte) (v >> 8);
		buf[count++] = (byte) (v);
	}

	@Override
	public void writeChar(int v) {
		writeShort(v);
	}

	@Override
	public void writeInt(int v) {
		ensureCapacity(4);
		byte[] buf = this.buf;
		int count = this.count;
		buf[count++] = (byte) (v >> 24);
		buf[count++] = (byte) (v >> 16);
		buf[count++] = (byte) (v >> 8);
		buf[count] = (byte) (v);
		this.count = count + 1;

	}

	@Override
	public void writeLong(long v) {
		ensureCapacity(8);
		byte[] buf = this.buf;
		int count = this.count;
		buf[count++] = (byte) (v >> 56);
		buf[count++] = (byte) (v >> 48);
		buf[count++] = (byte) (v >> 40);
		buf[count++] = (byte) (v >> 32);
		buf[count++] = (byte) (v >> 24);
		buf[count++] = (byte) (v >> 16);
		buf[count++] = (byte) (v >> 8);
		buf[count] = (byte) (v);
		this.count = count + 1;
	}

	@Override
	public void writeFloat(float v) {
		writeInt(Float.floatToIntBits(v));
	}

	@Override
	public void writeDouble(double v) {
		writeLong(Double.doubleToLongBits(v));
	}

	@Override
	public void writeChars(@NotNull String s) {
		int len = s.length();
		ensureCapacity(len << 1);
		int count = this.count;
		byte[] buf = this.buf;
		for (int i = 0; i < len; ++i) {
			char c = s.charAt(i);
			buf[count++] = (byte) (c >> 8);
			buf[count++] = (byte) (c);
		}
		this.count = count;
	}

	@Override
	public void writeUTF(@NotNull String s) {
		try {
			SCReflector.instance.writeUTF(null, s, this);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void writeBytes(@NotNull String s) {
		int len = s.length();
		ensureCapacity(len);
		byte[] buf = this.buf;
		int count = this.count;
		for (int i = 0; i < len; ++i) {
			buf[count++] = (byte) s.charAt(i);
		}
		this.count = count;
	}

	private static final int SEVEN_BITS = 0b0111_1111;
	private static final int BYTE_MSB = 0b1000_0000;

	@Override
	public void writeVarInt(int i) {
		while ((i & ~SEVEN_BITS) != 0) {
			writeByte(i & SEVEN_BITS | BYTE_MSB);
			i >>>= 7;
		}
		writeByte(i);
	}

	@Override
	public void writeString(String s) {
		writeVarInt(s.length());
		writeChars(s);
	}

	@Override
	public void writeItemStack(ItemStack stack) {
		if (stack == null) {
			writeShort(-1);
		} else {
			writeShort(stack.itemID);
			writeShort(stack.getItemDamage());
			writeByte(stack.stackSize);
			writeNBT(stack.stackTagCompound);
		}
	}

	@Override
	public void writeFluidStack(FluidStack stack) {
		if (stack == null) {
			writeVarInt(-1);
		} else {
			writeVarInt(stack.fluidID);
			writeVarInt(stack.amount);
			writeNBT(stack.tag);
		}
	}

	@Override
	public void writeNBT(NBTTagCompound nbt) {
		if (nbt == null) {
			writeByte(0);
		} else {
			writeByte(1);
			try {
				SCReflector.instance.write(nbt, this);
			} catch (IOException e) {
				throw new AssertionError(e);
			}
		}
	}

	@Override
	public void writeUUID(UUID uuid) {
		writeLong(uuid.getMostSignificantBits());
		writeLong(uuid.getLeastSignificantBits());
	}

	@Override
	public <E extends Enum<E>> void writeEnum(E e) {
		writeVarInt(e == null ? -1 : e.ordinal());
	}

	@Override
	public void writeBooleans(boolean[] booleans) {
		if (booleans == null) {
			writeVarInt(-1);
		} else {
			int len = booleans.length;
			writeVarInt(len);

			int numBytes = len % 8 + 1;
			ensureCapacity(numBytes);
			byte[] buf = this.buf;
			int count = this.count;

			int idx = 0;
			// write 8 booleans per byte
			// as long as we still have at least 8 elements left
			while (len - idx >= 8) {
				buf[count++] = (byte) ((booleans[idx] ? 0b0000_0001 : 0)
						| (booleans[idx + 1] ? 0b0000_0010 : 0)
						| (booleans[idx + 2] ? 0b0000_0100 : 0)
						| (booleans[idx + 3] ? 0b0000_1000 : 0)
						| (booleans[idx + 4] ? 0b0001_0000 : 0)
						| (booleans[idx + 5] ? 0b0010_0000 : 0)
						| (booleans[idx + 6] ? 0b0100_0000 : 0)
						| (booleans[idx + 7] ? 0b1000_0000 : 0));
				idx += 8;
			}
			// write any leftover elements in the array
			if (idx != len) {
				buf[count++] = (byte) ((booleans[idx] ? 0b0000_0001 : 0)
						| (idx + 1 < len && booleans[idx + 1] ? 0b0000_0010 : 0)
						| (idx + 2 < len && booleans[idx + 2] ? 0b0000_0100 : 0)
						| (idx + 3 < len && booleans[idx + 3] ? 0b0000_1000 : 0)
						| (idx + 4 < len && booleans[idx + 4] ? 0b0001_0000 : 0)
						| (idx + 5 < len && booleans[idx + 5] ? 0b0010_0000 : 0)
						| (idx + 6 < len && booleans[idx + 6] ? 0b0100_0000 : 0)
						| (idx + 7 < len && booleans[idx + 7] ? 0b1000_0000 : 0));
			}
			this.count = count;
		}
	}

	@Override
	public void writeBytes(byte[] bytes) {
		if (bytes == null) {
			writeVarInt(-1);
		} else {
			int len = bytes.length;
			writeVarInt(len);
			ensureCapacity(len);
			System.arraycopy(bytes, 0, buf, count, len);
			count += len;
		}
	}

	@Override
	public void writeShorts(short[] shorts) {
		if (shorts == null) {
			writeVarInt(-1);
		} else {
			// TODO make this fast!
		}
	}

	@Override
	public void writeInts(int[] ints) {

	}

	@Override
	public void writeLongs(long[] longs) {

	}

	@Override
	public void writeChars(char[] chars) {

	}

	@Override
	public void writeFloats(float[] floats) {

	}

	@Override
	public void writeDoubles(double[] doubles) {

	}

	@Override
	public byte[] toByteArray() {
		return Arrays.copyOf(buf, count);
	}

	@Override
	public void writeTo(OutputStream stream) throws IOException {
		stream.write(buf, 0, count);
	}

	@Override
	public void writeTo(DataOutput out) throws IOException {
		out.write(buf, 0, count);
	}
}
