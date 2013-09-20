package de.take_weiland.mods.commons.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.fluids.FluidStack;

public final class MinecraftDataInput extends InputStream implements DataInput {

	private final byte[] buf;
	private final int length;
	
	private int pos;
	
	public static MinecraftDataInput create(byte[] bytes) {
		checkNotNull(bytes, "Array can't be null");
		return new MinecraftDataInput(bytes, 0, bytes.length);
	}
	
	public static MinecraftDataInput create(byte[] bytes, int start, int length) {
		checkNotNull(bytes, "Array can't be null");
		return new MinecraftDataInput(bytes, start, length);
	}
	
	private MinecraftDataInput(byte[] bytes, int start, int length) {
		this.buf = bytes;
		this.pos = start;
		this.length = length;
	}
	
	private void checkAvailable(int n) {
		if (pos + n > length) {
			throw new IllegalStateException("Byte Array emptied!");
		}
	}
	
	private void checkAvailable() {
		if (pos >= length) {
			throw new IllegalStateException("Byte Array emptied!");
		}
	}
	
	// InputStream
	
	@Override
	public int read() {
		return pos < length ? buf[pos++] & 0xFF : -1;
	}

	@Override
	public int read(byte[] b) {
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) {
		if (b == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > b.length - off) {
			throw new IndexOutOfBoundsException();
		}

		if (pos >= length) {
			return -1;
		}

		int available = length - pos;
		if (len > available) {
			len = available;
		}
		if (len <= 0) {
			return 0;
		}
		System.arraycopy(buf, pos, b, off, len);
		pos += len;
		return len;
	}
	
	@Override
	public long skip(long n) {
		long available = length - pos;
		if (n < available) {
			available = n < 0 ? 0 : n;
		}

		pos += available;
		return available;
	}

	@Override
	public int available() {
		return length - pos;
	}

	// DataInput

	@Override
	public void readFully(byte[] b) {
		readFully(b, 0, b.length);
	}

	@Override
	public void readFully(byte[] b, int off, int len) {
		if (len < 0)
            throw new IndexOutOfBoundsException();
        int n = 0;
        while (n < len) {
            n += read(b, off + n, len - n);
        }
	}

	@Override
	public int skipBytes(int n) {
		int available = length - pos;
		if (n < available) {
			available = n < 0 ? 0 : n;
		}

		pos += available;
		return available;
	}
	
	@Override
	public int readUnsignedByte() {
		checkAvailable();
		return read();
	}
	
	@Override
	public byte readByte() {
		return (byte)readUnsignedByte();
	}

	@Override
	public boolean readBoolean() {
		return readByte() != 0;
	}

	@Override
	public short readShort() {
		checkAvailable(2);
		int b1 = read();
		int b2 = read();
		return (short)((b1 << 8) + (b2 << 0));
	}

	@Override
	public int readUnsignedShort() {
		checkAvailable(2);
		int b1 = read();
		int b2 = read();
		return (b1 << 8) + (b2 << 0);
	}

	@Override
	public char readChar() {
		checkAvailable(2);
		int b1 = read();
		int b2 = read();
		return (char) ((b1 << 8) + (b2 << 0));
	}

	@Override
	public int readInt() {
		checkAvailable(4);
		int b1 = read();
		int b2 = read();
		int b3 = read();
		int b4 = read();
		return ((b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0));
	}

	@Override
	public long readLong() {
		checkAvailable(8);
		int b1 = read();
		int b2 = read();
		int b3 = read();
		int b4 = read();
		int b5 = read();
		int b6 = read();
		int b7 = read();
		int b8 = read();
		return (((long) b1 << 56) +
				((long) (b2 & 255) << 48) +
				((long) (b3 & 255) << 40) + ((long) (b4 & 255) << 32) +
				((long) (b5 & 255) << 24) + ((b6 & 255) << 16) +
				((b7 & 255) << 8) + ((b8 & 255) << 0));
	}

	@Override
	public float readFloat() {
		return Float.intBitsToFloat(readInt());
	}

	@Override
	public double readDouble() {
		return Double.longBitsToDouble(readLong());
	}

	@Override
	@Deprecated
	public String readLine() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String readUTF() {
		try {
			return DataInputStream.readUTF(this);
		} catch (IOException e) {
			throw new AssertionError();
		}
	}

	public ItemStack readItemStack() {
		try {
			return Packet.readItemStack(this);
		} catch (IOException e) {
			throw new AssertionError();
		}
	}

	public FluidStack readFluidStack() {
		int fluidId = readShort();
		if (fluidId < 0) {
			return null;
		} else {
			int amount = readInt();
			NBTTagCompound nbt = readNBTTagCompound();
			return new FluidStack(fluidId, amount, nbt);
		}
	}

	public NBTTagCompound readNBTTagCompound() {
		int state = readUnsignedByte();
		try {
			switch (state) {
			case 1:
				return CompressedStreamTools.readCompressed(this);
			case 2:
				return CompressedStreamTools.read(this);
			default:
				return null;
			}
		} catch (IOException e) {
			throw new AssertionError();
		}
	}

	public <E extends Enum<E>> E readEnum(Class<E> clazz) {
		return CollectionUtils.safeArrayAccess(clazz.getEnumConstants(), readUnsignedByte());
	}

}
