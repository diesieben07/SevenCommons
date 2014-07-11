package de.take_weiland.mods.commons.net;

import com.google.common.base.Preconditions;
import com.google.common.primitives.*;
import de.take_weiland.mods.commons.util.ByteStreamSerializable;
import de.take_weiland.mods.commons.util.ByteStreamSerializer;
import de.take_weiland.mods.commons.util.UnsignedShorts;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.BufferUnderflowException;

class DataBufImpl implements DataBuf {

	// VarInt constants
	static final int first28Bits = 268435455;
	static final int first21Bits = 2097151;
	static final int first14Bits = 16383;
	static final int first7Bits = 127;
	static final int notFirst28Bits = ~first28Bits;
	static final int notFirst21Bits = ~first21Bits;
	static final int notFirst14Bits = ~first14Bits;
	static final int notFirst7Bits = ~first7Bits;

	/**
	 * backing buffer
	 */
	byte[] buf;
	/**
	 * current position in the array
	 */
	int pos;
	/**
	 * length of this buffer, might be bigger than the actual data that can be read via the get*** methods if this is a WritableDataBuf
	 */
	int len;
	/**
	 * non-oversized len, needed because WritableDataBuf oversizes the array on growth and available needs the actual len
	 */
	int actualLen;

	DataBufImpl(byte[] wrap, int off, int len) {
		this.buf = wrap;
		this.pos = off;
		this.len = actualLen = len;
	}

	@Override
	public boolean readBoolean() {
		return readByte() != 0;
	}

	@Override
	public byte readByte() {
		checkRemaining(1);
		return buf[pos++];
	}

	@Override
	public short readShort() {
		checkRemaining(2);
		int pos = this.pos;
		byte[] buf = this.buf;
		this.pos += 2;
		return Shorts.fromBytes(buf[pos], buf[pos + 1]);
	}

	@Override
	public int readInt() {
		checkRemaining(4);
		int pos = this.pos;
		byte[] buf = this.buf;
		this.pos += 4;
		return Ints.fromBytes(buf[pos], buf[pos + 1], buf[pos + 2], buf[pos + 3]);
	}

	@Override
	public long readLong() {
		checkRemaining(8);
		int pos = this.pos;
		byte[] buf = this.buf;
		this.pos += 8;
		return Longs.fromBytes(buf[pos], buf[pos + 1], buf[pos + 2], buf[pos + 3], buf[pos + 4], buf[pos + 5], buf[pos + 6], buf[pos + 7]);
	}

	@Override
	public char readChar() {
		checkRemaining(2);
		int pos = this.pos;
		byte[] buf = this.buf;
		this.pos += 2;
		return Chars.fromBytes(buf[pos], buf[pos + 1]);
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
	public int readVarInt() {
		int result = 0;
		int pass = 0;
		byte read;
		do {
			read = readByte();
			result |= (read & first7Bits) << pass++ * 7;
		} while ((read & 128) == 128);

		return result;
	}

	@Override
	public String readString() {
		int len = readVarInt();
		if (len < 0) {
			return null;
		}
		checkRemaining(len << 1);
		int pos = this.pos;
		byte[] buf = this.buf;
		char[] chars = new char[len];
		for (int i = 0; i < len; ++i) {
			int p = pos + (i << 1);
			chars[i] = Chars.fromBytes(buf[p], buf[p + 1]);
		}
		this.pos += len << 1;
		return new String(chars);
	}

	@Override
	public int readUnsignedByte() {
		return UnsignedBytes.toInt(readByte());
	}

	@Override
	public int readUnsignedShort() {
		return UnsignedShorts.toInt(readShort());
	}

	@Override
	public byte[] readByteArray() {
		int length = readVarInt();
		checkRemaining(length);
		byte[] result = new byte[length];
		System.arraycopy(buf, pos, result, 0, length);
		pos += length;
		return result;
	}

	@Override
	public byte[] readByteArray(byte[] target) {
		int len = readVarInt();
		checkRemaining(len);
		if (target.length < len) {
			target = new byte[len];
		}
		System.arraycopy(buf, pos, target, 0, len);
		pos += len;
		return target;
	}

	@Override
	public <T> T read(ByteStreamSerializer<T> serializer) {
		return serializer.read(this);
	}

	@Override
	public <T extends ByteStreamSerializable> T read(Class<T> clazz) {
		try {
			T o = clazz.newInstance();
			o.read(this);
			return o;
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Class " + clazz + " must have public no-arg constructor!", e);
		}
	}

	private InputStream inStreamView;

	@NotNull
	@Override
	public InputStream asInputStream() {
		return inStreamView == null ? (inStreamView = new DataBufAsInputstream(this)) : inStreamView;
	}

	private DataInput dataInputView;

	@NotNull
	@Override
	public DataInput asDataInput() {
		return dataInputView == null ? (dataInputView = new DataInputStream(asInputStream())) : dataInputView;
	}

	@Override
	public int available() {
		return actualLen - pos;
	}

	@Override
	public void seek(int pos) {
		Preconditions.checkPositionIndex(pos, actualLen, "Position");
		this.pos = pos;
	}

	@Override
	public int pos() {
		return pos;
	}

	@Override
	public int copyTo(@NotNull OutputStream out) throws IOException {
		return copyTo(out, -1);
	}

	@Override
	public int copyTo(@NotNull DataOutput out) throws IOException {
		return copyTo(out, -1);
	}

	@Override
	public int copyTo(@NotNull OutputStream out, int amount) throws IOException {
		amount = amount < 0 ? available() : Math.min(amount, available());
		out.write(buf, pos, amount);
		pos += amount;
		return amount;
	}

	@Override
	public int copyTo(@NotNull DataOutput out, int amount) throws IOException {
		amount = amount < 0 ? available() : Math.min(amount, available());
		out.write(buf, pos, amount);
		pos += amount;
		return amount;
	}

	@Override
	public int copyTo(@NotNull byte[] buf) {
		return copyTo(buf, 0, buf.length);
	}

	@Override
	public int copyTo(@NotNull byte[] buf, int off, int len) {
		len = Math.min(available(), len);
		System.arraycopy(this.buf, pos, buf, off, len);
		pos += len;
		return len;
	}

	private void checkRemaining(int i) {
		if (actualLen - pos < i) {
			throw new BufferUnderflowException();
		}
	}

}
