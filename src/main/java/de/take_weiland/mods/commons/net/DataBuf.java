package de.take_weiland.mods.commons.net;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface DataBuf {

	boolean getBoolean();
	byte getByte();
	short getShort();
	int getInt();
	long getLong();
	char getChar();
	float getFloat();
	double getDouble();
	
	String getString();
	
	int getVarInt();
	
	int getUnsignedByte();
	int getUnsignedShort();
	
	byte[] getByteArray();
	byte[] getByteArray(byte[] buf);
	
	int available();
	void seek(int pos);
	int pos();
	
	/**
	 * identical to {@link #copyTo(OutputStream, int) copyTo(out, -1)}
	 */
	int copyTo(OutputStream out) throws IOException;
	
	/**
	 * identical to {@link #copyTo(DataOutput, int) copyTo(out, -1)}
	 */
	int copyTo(DataOutput out) throws IOException;
	
	/**
	 * copy at most {@code amount} bytes starting from the current position to the OutputStream<br>
	 * if {@code amount} is negative all available bytes will be written
	 * @param out
	 * @param amount
	 * @return the amount of bytes actually copied
	 */
	int copyTo(OutputStream out, int amount) throws IOException;
	
	/**
	 * Simliar functionality to {@link #copyTo(OutputStream, int)}
	 */
	int copyTo(DataOutput out, int amount) throws IOException;
	
	/**
	 * copies at most {@code buf.length} bytes into {@code buf}
	 * @param buf
	 * @return the amount of bytes actually copied
	 */
	int copyTo(byte[] buf);
	
	/**
	 * copies at most {@code len} bytes into {@code buf}, starting at {@code buf[off]}
	 * @param buf
	 * @param off
	 * @param len
	 * @return the amount of bytes actually copied
	 */
	int copyTo(byte[] buf, int off, int len);
	
	InputStream asInputStream();
	DataInput asDataInput();
	
}
