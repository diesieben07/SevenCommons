package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.util.ByteStreamSerializable;
import de.take_weiland.mods.commons.util.ByteStreamSerializer;
import org.jetbrains.annotations.NotNull;

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
	boolean readBoolean();

	/**
	 * reads the next byte from the buffer
	 * @return the byte read
	 */
	byte readByte();

	/**
	 * reads the next short from the buffer
	 * @return the short read
	 */
	short readShort();

	/**
	 * reads the next int from the buffer
	 * @return the int read
	 */
	int readInt();

	/**
	 * reads the next long from the buffer
	 * @return the long read
	 */
	long readLong();

	/**
	 * reads the next char from the buffer
	 * @return the char read
	 */
	char readChar();

	/**
	 * reads the next float from the buffer
	 * @return the float read
	 */
	float readFloat();

	/**
	 * reads the next double from the buffer
	 * @return the double read
	 */
	double readDouble();

	/**
	 * reads the next String from the buffer
	 * @return the String read
	 */
	String readString();

	/**
	 * reads the next VarInt from the buffer
	 * @return the integer read
	 */
	int readVarInt();

	/**
	 * reads the next Unsigned byte from the buffer
	 * @return the integer read
	 */
	int readUnsignedByte();

	/**
	 * reads the next unsigned short from the buffer
	 * @return the integer read
	 */
	int readUnsignedShort();

	/**
	 * reads the next byte array from the buffer
	 * @return the byte array read
	 */
	byte[] readByteArray();

	/**
	 * reads the next byte array from the buffer, tries to use {@code buf} as a buffer, if big enough
	 * @param buf a pre-created array to hold the data
	 * @return the byte array read, doesn't need to be the same as {@code buf}
	 */
	byte[] readByteArray(byte[] buf);

	<T> T read(ByteStreamSerializer<T> serializer);

	<T extends ByteStreamSerializable> T read(Class<T> clazz);

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
	int copyTo(@NotNull OutputStream out) throws IOException;
	
	/**
	 * <p>Copies all bytes starting from the current position into the DataOuput.</p>
	 */
	int copyTo(@NotNull DataOutput out) throws IOException;
	
	/**
	 * <p>Copy at most {@code amount} bytes starting from the current position to the OutputStream</p>
	 * <p>If {@code amount} is negative all available bytes will be written.</p>
	 * @param out the Stream to copy to
	 * @param amount the amount of bytes to write
	 * @return the amount of bytes actually copied
	 */
	int copyTo(@NotNull OutputStream out, int amount) throws IOException;
	
	/**
	 * Simliar functionality to {@link #copyTo(OutputStream, int)}
	 */
	int copyTo(@NotNull DataOutput out, int amount) throws IOException;
	
	/**
	 * <p>Copies at most {@code buf.length} bytes into {@code buf}.</p>
	 * <p>If this buffer ends before {@code len} bytes have been copied, no further data is copied.</p>
	 * @param buf the buffer to copy into
	 * @return the amount of bytes actually copied
	 */
	int copyTo(@NotNull byte[] buf);
	
	/**
	 * <p>Copies at most {@code len} bytes into {@code buf}, so that the first element is put into {@code buf[off]}.</p>
	 * <p>If this buffer ends before {@code len} bytes have been copied, no further data is copied.</p>
	 * @param buf the buffer to copy into
	 * @param off the offset to start at
	 * @param len the number of bytes to copy
	 * @return the amount of bytes actually copied
	 */
	int copyTo(@NotNull byte[] buf, int off, int len);

	/**
	 * <p>View this buffer as an InputStream. Methods in the returned object read-through to this buffer.</p>
	 * @return an {@code InputStream} view of this buffer.
	 */
	@NotNull
	InputStream asInputStream();

	/**
	 * <p>View this buffer as a {@link java.io.DataInput}. Methods in the returned object read-through to this buffer.<GreendDiamond</p>
	 * <p>This method is preferred to {@code new DataInputStream(buf.asInputStream())}, even though they currently do the same.</p>
	 * @return a {@code DataInput} view of this buffer.
	 */
	@NotNull
	DataInput asDataInput();
	
}
