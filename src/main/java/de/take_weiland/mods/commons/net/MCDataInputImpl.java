package de.take_weiland.mods.commons.net;

import com.google.common.primitives.Ints;
import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.util.*;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkPositionIndexes;
import static de.take_weiland.mods.commons.net.MCDataOutputImpl.*;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
abstract class MCDataInputImpl extends MCDataInputStream implements MCDataInput {

	private static final int NO_MARK = -1;

	final byte[] buf;
	private final int maxLen;
	private final int initialPos;
	int pos;

	MCDataInputImpl(byte[] buf, int off, int len) {
		this.buf = buf;
		this.pos = this.initialPos = off;
		this.maxLen = len;
	}

	@Override
	public int read() {
		return (pos + initialPos) < maxLen ? buf[pos++] & 0xFF : -1;
	}

	@Override
	public int read(byte[] b, int off, int len) {
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
	public int read(byte[] b) {
		return read(b, 0, b.length);
	}

	@Override
	public void seek(int pos) {
		if (pos < 0) {
			throw new IllegalArgumentException("pos must be >= 0");
		} else if (pos > maxLen) {
			throw new IndexOutOfBoundsException("pos must be < length");
		} else {
			this.pos = (pos + initialPos);
		}
	}

	@Override
	public int pos() {
		return pos - initialPos;
	}

	@Override
	public int len() {
		return maxLen;
	}

	@Override
	public long skip(long n) {
		return skipBytes(Ints.saturatedCast(n));
	}

	@Override
	public int available() {
		return maxLen - pos;
	}

	@Override
	public void close() { }

	private int markedPos = NO_MARK;

	@Override
	public void mark(int readlimit) {
		markedPos = pos;
	}

	@Override
	public void reset() {
		if (markedPos == NO_MARK) {
			pos = initialPos;
		} else {
			pos = markedPos;
		}
	}

	final void checkAvailable(int bytes) {
		if (maxLen - pos < bytes) {
			throw new IllegalStateException("Read past end of buffer");
		}
	}

	@Override
	public InputStream asInputStream() {
		return this;
	}

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
		return readShort() & 0xFFFF;
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
		checkAvailable(1);
		return buf[pos++] & 0xFF;
	}

	@Override
	public byte readByte() {
		checkAvailable(1);
		return buf[pos++];
	}

	@Override
	public boolean readBoolean() {
		return readByte() != BOOLEAN_FALSE;
	}

	@Override
	public int skipBytes(int n) {
		if (n <= 0) {
			return 0;
		}
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

	@Override
	public BitSet readBitSet() {
		return readBitSet(null);
	}

	@Override
	public BitSet readBitSet(@Nullable BitSet bitSet) {
		long[] words = readLongs();
		if (words == null) {
			return null;
		} else if (bitSet == null) {
			return BufferUtils.bitSetHandler.createShared(words);
		} else {
			BufferUtils.bitSetHandler.updateInPlace(words, bitSet);
			return bitSet;
		}
	}

	@Override
	public <E extends Enum<E>> EnumSet<E> readEnumSet(Class<E> enumClass) {
		return BufferUtils.enumSetHandler.createShared(enumClass, readLong());
	}

	@Override
	public <E extends Enum<E>> EnumSet<E> readEnumSet(Class<E> enumClass, EnumSet<E> set) {
		return BufferUtils.enumSetHandler.update(enumClass, set, readLong());
	}

	@Override
	public <T> T read(ByteStreamSerializer<T> serializer) {
		return serializer.read(this);
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
			stack.stackTagCompound = readNBT();
			return stack;
		}
	}

	@Override
	public FluidStack readFluidStack() {
		int id = readVarInt();
		if (id < 0) {
			return null;
		} else {
			return new FluidStack(id, readVarInt(), readNBT());
		}
	}



	@Override
	public UUID readUUID() {
		long msb = readLong();
		if (msb == UUID_NULL_MSB) {
			return null;
		} else {
			return new UUID(msb, readLong());
		}
	}

	@Override
	public NBTTagCompound readNBT() {
		int id = readByte();
		if (id == -1) {
			return null;
		} else {
			NBTTagCompound nbt = new NBTTagCompound();
			Map<String, NBTBase> map = NBT.asMap(nbt);
			try {
				while (id != 0) {
					String name = readString();
					NBTBase tag = NBTBase.newTag((byte) id, name);
					SCReflector.instance.load(nbt, this, 1);

					map.put(tag.getName(), tag);
					id = readByte();
				}
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
			return nbt;
		}
	}

	@Override
	public Item readItem() {
		int id = readVarInt();
		try {
			return id == BufferUtils.ITEM_NULL_ID ? null : Item.itemsList[id];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalStateException("Invalid ItemID " + id);
		}
	}

	@Override
	public Block readBlock() {
		int id = readVarInt();
		try {
			return id == BufferUtils.BLOCK_NULL_ID ? null : Block.blocksList[id];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalStateException("Invalid BlockID " + id);
		}
	}

	@Override
	public BlockPos readCoords() {
		return BlockPos.streamSerializer().read(this);
	}

	@Override
	public Boolean readBooleanBox() {
		int b = readByte();
		return b == BOOLEAN_NULL ? null : b != BOOLEAN_FALSE;
	}

	@Override
	public Byte readByteBox() {
		if (readByte() == BOX_NULL) {
			return null;
		} else {
			return readByte();
		}
	}

	@Override
	public Short readShortBox() {
		if (readByte() == BOX_NULL) {
			return null;
		} else {
			return readShort();
		}
	}

	@Override
	public Character readCharBox() {
		if (readByte() == BOX_NULL) {
			return null;
		} else {
			return readChar();
		}
	}

	@Override
	public Integer readIntBox() {
		if (readByte() == BOX_NULL) {
			return null;
		} else {
			return readInt();
		}
	}

	@Override
	public Long readLongBox() {
		if (readByte() == BOX_NULL) {
			return null;
		} else {
			return readLong();
		}
	}

	@Override
	public Float readFloatBox() {
		if (readByte() == BOX_NULL) {
			return null;
		} else {
			return readFloat();
		}
	}

	@Override
	public Double readDoubleBox() {
		if (readByte() == BOX_NULL) {
			return null;
		} else {
			return readDouble();
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
			if (b == null || b.length != len) {
				b = new boolean[len];
			}
			int byteLen = (len - 1) / 8 + 1; // works for len > 0
			checkAvailable(byteLen);

			byte[] buf = this.buf;
			int pos = this.pos;

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
			this.pos = pos;

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
			if (b == null || b.length != len) {
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
