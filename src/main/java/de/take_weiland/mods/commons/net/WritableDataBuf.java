package de.take_weiland.mods.commons.net;

import java.io.DataOutput;
import java.io.OutputStream;

public interface WritableDataBuf extends DataBuf {

	WritableDataBuf putBoolean(boolean b);
	WritableDataBuf putByte(byte b);
	WritableDataBuf putShort(short s);
	WritableDataBuf putInt(int i);
	WritableDataBuf putLong(long l);
	WritableDataBuf putChar(char c);
	WritableDataBuf putFloat(float f);
	WritableDataBuf putDouble(double d);
	
	WritableDataBuf putString(String s);
	
	WritableDataBuf putVarInt(int i);
	WritableDataBuf putUnsignedByte(int i);
	WritableDataBuf putUnsignedShort(int i);
	
	WritableDataBuf put(byte[] bytes);
	WritableDataBuf put(byte[] bytes, int off, int len);
	
	/**
	 * grows the internal buffer so that it can hold at least n additional bytes<br>
	 * repeated calls to this method do not accumulate, e.g.:
	 * <pre>{@code
	 * buf.grow(5);
	 *buf.grow(7); }</pre>will only grow the buffer to hold 7 additional bytes, not 12.
	 * @param n
	 */
	WritableDataBuf grow(int n);
	
	OutputStream asOutputStream();
	DataOutput asDataOutput();
	
}
