package de.take_weiland.mods.commons.net;

import com.google.common.primitives.UnsignedBytes;
import de.take_weiland.mods.commons.util.ByteStreamSerializable;
import de.take_weiland.mods.commons.util.ByteStreamSerializer;
import de.take_weiland.mods.commons.util.UnsignedShorts;
import org.bouncycastle.util.Arrays;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.OutputStream;

@SuppressWarnings("unchecked")
abstract class WritableDataBufImpl<SELF extends WritableDataBufImpl<SELF>> extends DataBufImpl implements WritableDataBuf {

	WritableDataBufImpl(byte[] wrap) {
		super(wrap, 0, wrap.length);
		actualLen = 0;
	}
	
	private static final byte BOOL_FALSE = 0;
	private static final byte BOOL_TRUE = 1;

	@Override
	public SELF writeBoolean(boolean b) {
		return writeByte(b ? BOOL_TRUE : BOOL_FALSE);
	}

	@Override
	public SELF writeByte(int b) {
		grow0(1);
		buf[pos++] = (byte) b;
		return (SELF) this;
	}

	@Override
	public SELF writeShort(int s) {
		grow0(2);
		int pos = this.pos;
		byte[] buf = this.buf;
		buf[pos] = (byte) ((s >>> 8) & 0xff);
		buf[pos + 1] = (byte) (s &0xff);
		this.pos += 2;
		return (SELF) this;
	}

	@Override
	public SELF writeInt(int i) {
		grow0(4);
		int pos = this.pos;
		byte[] buf = this.buf;
		buf[pos] = (byte)((i >>> 24) & 0xff);
		buf[pos + 1] = (byte) ((i >>> 16) & 0xff);
		buf[pos + 2] = (byte) ((i >>>  8) & 0xff);
		buf[pos + 3] = (byte) (i & 0xff);
		this.pos += 4;
		return (SELF) this;
	}

	@Override
	public SELF writeLong(long l) {
		grow0(8);
		int pos = this.pos;
		byte[] buf = this.buf;
		buf[pos]     = (byte)(l >>> 56);
		buf[pos + 1] = (byte)(l >>> 48);
		buf[pos + 2] = (byte)(l >>> 40);
		buf[pos + 3] = (byte)(l >>> 32);
		buf[pos + 4] = (byte)(l >>> 24);
		buf[pos + 5] = (byte)(l >>> 16);
		buf[pos + 6] = (byte)(l >>>  8);
		buf[pos + 7] = (byte)(l >>>  0);
		this.pos += 8;
		return (SELF) this;
	}

	@Override
	public SELF writeChar(char c) {
		grow0(2);
		int pos = this.pos;
		byte[] buf = this.buf;
		buf[pos] = (byte) ((c >>> 8) & 0xff);
		buf[pos + 1] = (byte) (c & 0xff);
		this.pos += 2;
		return (SELF) this;
	}

	@Override
	public SELF writeFloat(float f) {
		return writeInt(Float.floatToIntBits(f));
	}

	@Override
	public SELF writeDouble(double d) {
		return writeLong(Double.doubleToLongBits(d));
	}
	
	private static int varIntSize(int i) {
		return ((i & notFirst7Bits) == 0) ? 1 : ((i & notFirst14Bits) == 0) ? 2 : ((i & notFirst21Bits) == 0) ? 3 : ((i & notFirst28Bits) == 0) ? 4 : 5;
	}

	@Override
	public SELF writeVarInt(int i) {
		int size = varIntSize(i);
		grow0(size);
		int pos = this.pos;
		byte[] buf = this.buf;
		while ((i & notFirst7Bits) != 0) {
			buf[pos++] = (byte) (i & first7Bits | 128);
			i >>>= 7;
		}
		buf[pos] = (byte) (i & first7Bits);
		this.pos += size;
		return (SELF) this;
	}

	@Override
	public SELF writeUnsignedByte(int i) {
		return writeByte(UnsignedBytes.checkedCast(i));
	}

	@Override
	public SELF writeUnsignedShort(int i) {
		return writeShort(UnsignedShorts.checkedCast(i));
	}

	@Override
	@Deprecated
	public SELF writeString(String s) {
		if (s == null) {
			return writeVarInt(-1);
		}
		int len = s.length();
		writeVarInt(len);
		grow0(len << 1);
		int pos = this.pos;
		byte[] buf = this.buf;
		for (int i = 0; i < len; ++i) {
			char c = s.charAt(i);
			int p = pos + (i << 1);
			buf[p] = (byte) ((c >>> 8) & 0xff);
			buf[p + 1] = (byte) (c & 0xff);
		}
		this.pos += len << 1;
		return (SELF) this;
	}

	@Override
	public SELF writeBytes(byte[] bytes) {
		if (bytes == null) {
			writeVarInt(-1);
		} else {
			writeVarInt(bytes.length);
			writeRaw(bytes);
		}
		return (SELF) this;
	}

	@Override
	public SELF writeRaw(byte[] bytes) {
		return writeRaw(bytes, 0, bytes.length);
	}

	@Override
	public SELF writeRaw(byte[] arr, int off, int len) {
		DataBuffers.validateArray(arr, off, len);
        grow0(len);
        System.arraycopy(arr, off, buf, pos, len);
        pos += len;
		return (SELF) this;
	}

	@Override
	public <T> SELF write(T obj, ByteStreamSerializer<? super T> serializer) {
		serializer.write(obj, this);
		return (SELF) this;
	}

	@Override
	public SELF write(ByteStreamSerializable o) {
		o.write(this);
		return (SELF) this;
	}

	private OutputStream outStreamView;
	
	@Override
	public OutputStream asOutputStream() {
		return outStreamView == null ? (outStreamView = new DataBufAsOutputStream(this)) : outStreamView;
	}
	
	private DataOutput dataOutputView;
	
	@Override
	public DataOutput asDataOutput() {
		return dataOutputView == null ? (dataOutputView = new DataOutputStream(asOutputStream())) : dataOutputView;
	}

	@Override
	public SELF grow(int n) {
		grow0(n);
		return (SELF) this;
	}

	@Override
	public byte[] toByteArray() {
		return Arrays.copyOf(buf, actualLen);
	}

	void grow0(int i) {
		if (len - pos >= i) {
			actualLen += i;
			return;
		}
		int len = this.len;
		len += Math.max(len >> 1, i);
		if (len < 0) {
			throw new OutOfMemoryError();
		}
		buf = Arrays.copyOf(buf, (this.len = len));
		actualLen += i;
	}

}
