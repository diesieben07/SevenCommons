package de.take_weiland.mods.commons.net;

import com.google.common.primitives.UnsignedBytes;
import de.take_weiland.mods.commons.util.UnsignedShorts;
import org.bouncycastle.util.Arrays;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.OutputStream;

class WritableDataBufImpl<TYPE extends Enum<TYPE>> extends DataBufImpl implements PacketBuilder {

	WritableDataBufImpl(byte[] wrap) {
		super(wrap, 0, wrap.length);
		actualLen = 0;
	}
	
	private static final byte BOOL_FALSE = 0;
	private static final byte BOOL_TRUE = 1;

	@Override
	public PacketBuilder putBoolean(boolean b) {
		return putByte(b ? BOOL_TRUE : BOOL_FALSE);
	}

	@Override
	public PacketBuilder putByte(int b) {
		grow0(1);
		buf[pos++] = (byte) b;
		return this;
	}

	@Override
	public PacketBuilder putShort(int s) {
		grow0(2);
		int pos = this.pos;
		byte[] buf = this.buf;
		buf[pos] = (byte) ((s >>> 8) & 0xff);
		buf[pos + 1] = (byte) (s &0xff);
		this.pos += 2;
		return this;
	}

	@Override
	public PacketBuilder putInt(int i) {
		grow0(4);
		int pos = this.pos;
		byte[] buf = this.buf;
		buf[pos] = (byte)((i >>> 24) & 0xff);
		buf[pos + 1] = (byte) ((i >>> 16) & 0xff);
		buf[pos + 2] = (byte) ((i >>>  8) & 0xff);
		buf[pos + 3] = (byte) (i & 0xff);
		this.pos += 4;
		return this;
	}

	@Override
	public PacketBuilder putLong(long l) {
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
		return this;
	}

	@Override
	public PacketBuilder putChar(char c) {
		grow0(2);
		int pos = this.pos;
		byte[] buf = this.buf;
		buf[pos] = (byte) ((c >>> 8) & 0xff);
		buf[pos + 1] = (byte) (c & 0xff);
		this.pos += 2;
		return this;
	}

	@Override
	public PacketBuilder putFloat(float f) {
		return putInt(Float.floatToIntBits(f));
	}

	@Override
	public PacketBuilder putDouble(double d) {
		return putLong(Double.doubleToLongBits(d));
	}
	
	private static int varIntSize(int i) {
		return ((i & notFirst7Bits) == 0) ? 1 : ((i & notFirst14Bits) == 0) ? 2 : ((i & notFirst21Bits) == 0) ? 3 : ((i & notFirst28Bits) == 0) ? 4 : 5;
	}

	@Override
	public PacketBuilder putVarInt(int i) {
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
		return this;
	}

	@Override
	public PacketBuilder putUnsignedByte(int i) {
		return putByte(UnsignedBytes.checkedCast(i));
	}

	@Override
	public PacketBuilder putUnsignedShort(int i) {
		return putShort(UnsignedShorts.checkedCast(i));
	}

	@Override
	public PacketBuilder putString(String s) {
		if (s == null) {
			return putVarInt(-1);
		}
		int len = s.length();
		putVarInt(len);
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
		return this;
	}

	@Override
	public PacketBuilder putBytes(byte[] bytes) {
		putVarInt(bytes.length);
		putRaw(bytes);
		return null;
	}

	@Override
	public PacketBuilder putRaw(byte[] bytes) {
		return putRaw(bytes, 0, bytes.length);
	}

	@Override
	public PacketBuilder putRaw(byte[] arr, int off, int len) {
		DataBuffers.validateArray(arr, off, len);
        grow0(len);
        System.arraycopy(arr, off, buf, pos, len);
        pos += len;
		return this;
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
	public PacketBuilder grow(int n) {
		grow0(n);
		return this;
	}
	
	TYPE type;
	PacketFactoryInternal<TYPE> packetFactory;
	@Override
	public SimplePacket toPacket() {
		return packetFactory.make(this);
	}
	
	private void grow0(int i) {
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
