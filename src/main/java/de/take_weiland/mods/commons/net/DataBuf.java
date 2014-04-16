package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.util.ByteStreamSerializable;
import de.take_weiland.mods.commons.util.ByteStreamSerializer;

import java.io.*;

/**
 * <p>a Buffer backed by a byte[]. Usually used in conjuction with packets</p>
 * <p>The methods in this class expect the data in the format produced by a {@link de.take_weiland.mods.commons.net.WritableDataBuf}</p>
 * <p>Obtain instances with the {@link de.take_weiland.mods.commons.net.DataBuffers} class.</p>
 */
public interface DataBuf {

	/**
	 * reads a byte from the buffer and returns true if it is not 0.
	 * @return true if the read byte is not 0
	 */
	boolean getBoolean();

	/**
	 * reads the next byte from the buffer
	 * @return the byte read
	 */
	byte getByte();

	/**
	 * reads the next short from the buffer
	 * @return the short read
	 */
	short getShort();

	/**
	 * reads the next int from the buffer
	 * @return the int read
	 */
	int getInt();

	/**
	 * reads the next long from the buffer
	 * @return the long read
	 */
	long getLong();

	/**
	 * reads the next char from the buffer
	 * @return the char read
	 */
	char getChar();

	/**
	 * reads the next float from the buffer
	 * @return the float read
	 */
	float getFloat();

	/**
	 * reads the next double from the buffer
	 * @return the double read
	 */
	double getDouble();

	/**
	 * reads the next String from the buffer
	 * @return the String read
	 */
	String getString();

	/**
	 * reads the next VarInt from the buffer
	 * @return the integer read
	 */
	int getVarInt();

	/**
	 * reads the next Unsigned byte from the buffer
	 * @return the integer read
	 */
	int getUnsignedByte();

	/**
	 * reads the next unsigned short from the buffer
	 * @return the integer read
	 */
	int getUnsignedShort();

	/**
	 * reads the next byte array from the buffer
	 * @return the byte array read
	 */
	byte[] getByteArray();

	/**
	 * reads the next byte array from the buffer, tries to use {@code buf} as a buffer, if big enough
	 * @param buf a pre-created array to hold the data
	 * @return the byte array read, doesn't need to be the same as {@code buf}
	 */
	byte[] getByteArray(byte[] buf);

	<T> T get(ByteStreamSerializer<T> serializer);

	<T extends ByteStreamSerializable> T get(Class<T> clazz);

	/**
	 * how many bytes can still be read from this buffer, starting at the current position
	 * @return number of available bytes
	 */
	int available();

	/**
	 * sets the current position
	 * @param pos the new position
	 */
	void seek(int pos);

	/**
	 * gets the current position
	 * @return the current position
	 */
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

	/**
	 * view this buffer as an InputStream. Methods in the returned object read-through to this buffer
	 * @return an InputStream
	 */
	InputStream asInputStream();

	/**
	 * view this buffer as a DataInput. Methods in the returned object read-through to this buffer.
	 * This method is preferred to {@code new DataInputStream(buf.asInputStream())}, even though they currently do the same.
	 * @return
	 */
	DataInput asDataInput();
	
}
