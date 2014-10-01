package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.util.BlockCoordinates;
import de.take_weiland.mods.commons.util.ByteStreamSerializable;
import de.take_weiland.mods.commons.util.SCReflector;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UTFDataFormatException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkPositionIndexes;

/**
 * @author diesieben07
 */
abstract class MCDataOutputImpl extends MCDataOutputStream {

	static final int BOOLEAN_NULL = -1;
	static final int BOOLEAN_TRUE = 1;
	static final int BOOLEAN_FALSE = 0;

	static final int BOX_NULL = 0;
	static final int BOX_NONNULL = 1;

	private boolean locked = false;
	byte[] buf;
	int count;

	MCDataOutputImpl(int initialCap) {
		checkArgument(initialCap >= 0, "negative initial size");
		buf = new byte[initialCap];
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

	@Override
	public void writeTo(ByteBuffer buf) {
		buf.put(this.buf, 0, count);
	}

	@Override
	public void writeTo(WritableByteChannel channel) throws IOException {
		channel.write(ByteBuffer.wrap(buf, 0, count));
	}

	@Override
	public byte[] backingArray() {
		return buf;
	}

	@Override
	public int length() {
		return count;
	}

	@Override
	public void lock() {
		locked = true;
	}

	@Override
	public boolean isLocked() {
		return locked;
	}

	final void ensureWritable(int bytesToWrite) {
		if (locked) {
			throw new IllegalStateException("Output locked!");
		}
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
		ensureWritable(1);
		buf[count++] = (byte) b;
	}

	@Override
	public void write(@NotNull byte[] b) {
		write(b, 0, b.length);
	}

	@Override
	public void write(@NotNull byte[] b, int off, int len) {
		ensureWritable(len);
		System.arraycopy(b, off, buf, count, len);
		count += len;
	}

	@Override
	public void flush() { }

	@Override
	public void close() { }

	@Override
	public void writeBoolean(boolean v) {
		write(v ? BOOLEAN_TRUE : BOOLEAN_FALSE);
	}

	@Override
	public void writeByte(int v) {
		write(v);
	}

	@Override
	public void writeShort(int v) {
		ensureWritable(2);
		buf[count++] = (byte) (v);
		buf[count++] = (byte) (v >> 8);
	}

	@Override
	public void writeChar(int v) {
		writeShort(v);
	}

	@Override
	public void writeInt(int v) {
		ensureWritable(4);
		byte[] buf = this.buf;
		int count = this.count;
		buf[count++] = (byte) (v);
		buf[count++] = (byte) (v >> 8);
		buf[count++] = (byte) (v >> 16);
		buf[count] = (byte) (v >> 24);
		this.count = count + 1;

	}

	@Override
	public void writeLong(long v) {
		ensureWritable(8);
		byte[] buf = this.buf;
		int count = this.count;
		buf[count++] = (byte) (v);
		buf[count++] = (byte) (v >> 8);
		buf[count++] = (byte) (v >> 16);
		buf[count++] = (byte) (v >> 24);
		buf[count++] = (byte) (v >> 32);
		buf[count++] = (byte) (v >> 40);
		buf[count++] = (byte) (v >> 48);
		buf[count] = (byte) (v >> 56);
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
		ensureWritable(len << 1);
		int count = this.count;
		byte[] buf = this.buf;
		for (int i = 0; i < len; ++i) {
			char c = s.charAt(i);
			buf[count++] = (byte) (c);
			buf[count++] = (byte) (c >> 8);
		}
		this.count = count;
	}

	@Override
	public void writeUTF(@NotNull String s) {
		char c;
		int i;
		int utfLen = 0;
		int strLen = s.length();
		for (i = 0; i < strLen; i++) {
			c = s.charAt(i);
			if (c >= 0x0001 && c <= 0x007f) {
				utfLen++;
			} else if (c >= 0x800) {
				utfLen += 3;
			} else {
				utfLen += 2;
			}
		}
		if (utfLen > 65535) {
			throw new IllegalStateException(new UTFDataFormatException("Encoded String too long. " + utfLen + " > 65535"));
		}
		writeShort(utfLen);
		ensureWritable(utfLen);

		byte[] buf = this.buf;
		int count = this.count;

		if (utfLen == strLen) {
			for (i = 0; i < strLen; i++) {
				buf[count++] = (byte) s.charAt(i);
			}
		} else {
			for (i = 0; i < strLen; i++) {
				c = s.charAt(i);
				if (c >= 0x0001 && c <= 0x007f) {
					buf[count++] = (byte) c;
				} else if (c >= 0x800) {
					buf[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
					buf[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
					buf[count++] = (byte) (0x80 | (c & 0x3F));
				} else {
					buf[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
					buf[count++] = (byte) (0x80 | (c & 0x3F));
				}
			}
			this.count = count;
		}
	}

	@Override
	@Deprecated
	public void writeBytes(@NotNull String s) {
		int len = s.length();
		ensureWritable(len);
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
			writeByte(i & SEVEN_BITS);
			i >>>= 7;
		}
		writeByte(i | (BYTE_MSB));
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
			writeByte(-1);
		} else {
			try {
				for (NBTBase tag : NBT.asMap(nbt).values()) {
					writeByte(tag.getId());
					writeString(tag.getName());
					SCReflector.instance.write(tag, this);
				}
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	@Override
	public void writeItem(Item item) {
		writeVarInt(item == null ? BufferUtils.ITEM_NULL_ID : item.itemID);
	}

	@Override
	public void writeBlock(Block block) {
		writeVarInt(block == null ? BufferUtils.BLOCK_NULL_ID : block.blockID);
	}

	@Override
	public void writeCoords(int x, int y, int z) {
		BlockCoordinates.toByteStream(this, x, y, z);
	}

	@Override
	public void write(ByteStreamSerializable obj) {
		obj.writeTo(this);
	}

	static final int UUID_FAKE_NULL_VERSION = 0xF000;
	static final int UUID_VERSION_MASK = 0xF000;

	@Override
	public void writeUUID(UUID uuid) {
		if (uuid == null) {
			writeShort(UUID_FAKE_NULL_VERSION);
		} else {
			writeLong(uuid.getMostSignificantBits());
			writeLong(uuid.getLeastSignificantBits());
		}
	}

	@Override
	public <E extends Enum<E>> void writeEnum(E e) {
		writeVarInt(e == null ? -1 : e.ordinal());
	}

	@Override
	public void writeBitSet(BitSet bitSet) {
		if (bitSet == null) {
			writeLongs(null);
		} else {
			BufferUtils.bitSetHandler.writeTo(bitSet, this);
		}
	}

	static final long ENUM_SET_NULL = 1L << 63L;

	@Override
	public <E extends Enum<E>> void writeEnumSet(EnumSet<E> enumSet) {
		if (enumSet == null) {
			writeLong(ENUM_SET_NULL);
		} else if (enumSet.size() == 0) {
			writeLong(0);
		} else {
			long l = 0;
			for (E e : enumSet) {
				l |= (1L << e.ordinal());
			}
			writeLong(l);
		}
	}

	@Override
	public void writeBooleanBox(Boolean b) {
		writeByte(b == null ? BOOLEAN_NULL : (b ? BOOLEAN_TRUE : BOOLEAN_FALSE));
	}

	@Override
	public void writeByteBox(Byte b) {
		if (b == null) {
			writeByte(BOX_NULL);
		} else {
			writeByte(BOX_NONNULL);
			writeByte(b);
		}
	}

	@Override
	public void writeShortBox(Short s) {
		if (s == null) {
			writeByte(BOX_NULL);
		} else {
			writeByte(BOX_NONNULL);
			writeShort(s);
		}
	}

	@Override
	public void writeCharBox(Character c) {
		if (c == null) {
			writeByte(BOX_NULL);
		} else {
			writeByte(BOX_NONNULL);
			writeChar(c);
		}
	}

	@Override
	public void writeIntBox(Integer i) {
		if (i == null) {
			writeByte(BOX_NULL);
		} else {
			writeByte(BOX_NONNULL);
			writeInt(i);
		}
	}

	@Override
	public void writeLongBox(Long l) {
		if (l == null) {
			writeByte(BOX_NULL);
		} else {
			writeByte(BOX_NONNULL);
			writeLong(l);
		}
	}

	@Override
	public void writeFloatBox(Float f) {
		if (f == null) {
			writeByte(BOX_NULL);
		} else {
			writeByte(BOX_NONNULL);
			writeFloat(f);
		}
	}

	@Override
	public void writeDoubleBox(Double d) {
		if (d == null) {
			writeByte(BOX_NULL);
		} else {
			writeByte(BOX_NONNULL);
			writeDouble(d);
		}
	}

	@Override
	public void writeBooleans(boolean[] booleans, int off, int len) {
		checkPositionIndexes(off, off + len, booleans.length);
		writeBooleans0(booleans, off, len);
	}

	@Override
	public void writeBooleans(boolean[] booleans) {
		if (booleans == null) {
			writeVarInt(-1);
		} else {
			writeBooleans0(booleans, 0, booleans.length);
		}
	}

	private void writeBooleans0(boolean[] booleans, int off, int len) {
		writeVarInt(len);
		ensureWritable(len % 8 + 1);
		writeBooleans00(booleans, off, len);
		count += len % 8 + 1;
	}

	abstract void writeBooleans00(boolean[] booleans, int off, int len);

	@Override
	public void writeBytes(byte[] bytes, int off, int len) {
		checkArgument(len >= 0, "len must be >= 0");
		checkPositionIndexes(off, off + len, bytes.length);

		writeBytes0(bytes, off, len);
	}

	@Override
	public void writeBytes(byte[] bytes) {
		if (bytes == null) {
			writeVarInt(-1);
		} else {
			writeBytes0(bytes, 0, bytes.length);
		}
	}

	private void writeBytes0(byte[] bytes, int off, int len) {
		writeVarInt(len);
		ensureWritable(len);
		System.arraycopy(bytes, off, buf, count, len);
		count += len;
	}

	@Override
	public void writeShorts(short[] shorts) {
		if (shorts == null) {
			writeVarInt(-1);
		} else {
			writeShorts0(shorts, 0, shorts.length);
		}
	}

	@Override
	public void writeShorts(short[] shorts, int off, int len) {
		if (shorts == null) {
			writeVarInt(-1);
		} else {
			checkArgument(len >= 0, "len must be >= 0");
			checkPositionIndexes(off, off + len, shorts.length);
			writeShorts0(shorts, off, len);
		}
	}

	private void writeShorts0(short[] shorts, int off, int len) {
		writeVarInt(len);
		ensureWritable(len << 1);
		writeShorts00(shorts, off, len);
		count += len << 1;
	}

	abstract void writeShorts00(short[] shorts, int off, int len);

	@Override
	public void writeInts(int[] ints) {
		if (ints == null) {
			writeVarInt(-1);
		} else {
			writeInts0(ints, 0, ints.length);
		}
	}

	@Override
	public void writeInts(int[] ints, int off, int len) {
		if (ints == null) {
			writeVarInt(-1);
		} else {
			checkArgument(len >= 0, "len must be >= 0");
			checkPositionIndexes(off, off + len, ints.length);
			writeInts0(ints, off, len);
		}
	}

	private void writeInts0(int[] ints, int off, int len) {
		writeVarInt(len);
		ensureWritable(len << 2);
		writeInts00(ints, off, len);
		count += len << 2;
	}

	abstract void writeInts00(int[] ints, int off, int len);

	@Override
	public void writeLongs(long[] longs) {
		if (longs == null) {
			writeVarInt(-1);
		} else {
			writeLongs0(longs, 0, longs.length);
		}
	}

	@Override
	public void writeLongs(long[] longs, int off, int len) {
		if (longs == null) {
			writeVarInt(-1);
		} else {
			checkArgument(len >= 0, "len must be >= 0");
			checkPositionIndexes(off, off + len, longs.length);
			writeLongs0(longs, off, len);
		}
	}

	private void writeLongs0(long[] longs, int off, int len) {
		writeVarInt(len);
		ensureWritable(len << 3);
		writeLongs00(longs, off, len);
		count += len << 3;
	}

	abstract void writeLongs00(long[] longs, int off, int len);

	@Override
	public void writeChars(char[] chars) {
		if (chars == null) {
			writeVarInt(-1);
		} else {
			writeChars0(chars, 0, chars.length);
		}
	}

	@Override
	public void writeChars(char[] chars, int off, int len) {
		if (chars == null) {
			writeVarInt(-1);
		} else {
			checkArgument(len >= 0, "len must be >= 0");
			checkPositionIndexes(off, off + len, chars.length);
			writeChars0(chars, off, len);
		}
	}

	private void writeChars0(char[] chars, int off, int len) {
		writeVarInt(len);
		ensureWritable(len << 1);
		writeChars00(chars, off, len);
		count += len << 1;
	}

	abstract void writeChars00(char[] chars, int off, int len);

	@Override
	public void writeFloats(float[] floats, int off, int len) {
		if (floats == null) {
			writeVarInt(-1);
		} else {
			checkArgument(len >= 0, "len must be >= 0");
			checkPositionIndexes(off, off + len, floats.length);
			writeFloats0(floats, off, len);
		}
	}

	@Override
	public void writeFloats(float[] floats) {
		if (floats == null) {
			writeVarInt(-1);
		} else {
			writeFloats0(floats, 0, floats.length);
		}
	}

	private void writeFloats0(float[] floats, int off, int len) {
		writeVarInt(len);
		ensureWritable(len << 2);
		writeFloats00(floats, off, len);
		count += len << 2;
	}

	abstract void writeFloats00(float[] floats, int off, int len);

	@Override
	public void writeDoubles(double[] doubles, int off, int len) {
		if (doubles == null) {
			writeVarInt(-1);
		} else {
			checkArgument(len >= 0, "len must be >= 0");
			checkPositionIndexes(off, off + len, doubles.length);
			writeDoubles0(doubles, off, len);
		}
	}

	@Override
	public void writeDoubles(double[] doubles) {
		if (doubles == null) {
			writeVarInt(-1);
		} else {
			writeDoubles0(doubles, 0, doubles.length);
		}
	}

	private void writeDoubles0(double[] doubles, int off, int len) {
		writeVarInt(len);
		ensureWritable(len << 3);
		writeDoubles00(doubles, off, len);
		count += len << 3;
	}

	abstract void writeDoubles00(double[] doubles, int off, int len);

}
