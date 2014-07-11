package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.util.ByteStreamSerializable;
import de.take_weiland.mods.commons.util.ByteStreamSerializer;

import java.io.DataOutput;
import java.io.OutputStream;

/**
 * <p>a DataBuffer which you can writePacketId to, self-growing</p>
 * <p>Obtain instances with the {@link de.take_weiland.mods.commons.net.DataBuffers} class.</p>
 */
public interface WritableDataBuf extends DataBuf {

	/**
	 * writes a byte to the buffer, which is 0 if b == false and 1 if b == true.
	 *
	 * @param b the boolean to writePacketId
	 * @return this
	 */
	WritableDataBuf writeBoolean(boolean b);

	/**
	 * writes the lowest 8 bits of the given integer to the buffer as a byte
	 *
	 * @param b the byte to writePacketId
	 * @return this
	 */
	WritableDataBuf writeByte(int b);

	/**
	 * writes the lowest 16 bits of the given integer to the buffer as a short (2 bytes)
	 *
	 * @param s the short to writePacketId
	 * @return this
	 */
	WritableDataBuf writeShort(int s);

	/**
	 * writes the given integer to the buffer as 4 bytes
	 *
	 * @param i the integer to writePacketId
	 * @return this
	 */
	WritableDataBuf writeInt(int i);

	/**
	 * writes the given long to the buffer as 8 bytes
	 *
	 * @param l the long to writePacketId
	 * @return this
	 */
	WritableDataBuf writeLong(long l);

	/**
	 * writes the given character to the buffer as 2 bytes
	 *
	 * @param c the character to writePacketId
	 * @return this
	 */
	WritableDataBuf writeChar(char c);

	/**
	 * writes the given float to the buffer as an int
	 *
	 * @param f the float to writePacketId
	 * @return this
	 */
	WritableDataBuf writeFloat(float f);

	/**
	 * writes the given double to the buffer as a long
	 *
	 * @param d the double to writePacketId
	 * @return this
	 */
	WritableDataBuf writeDouble(double d);

	/**
	 * <p>Writes only as many bytes as needed to represent the given integer to the buffer. This can save network bandwidth,
	 * if the integer being written is usually in the lower range.</p>
	 *
	 * @param i the integer to writePacketId
	 * @return this
	 */
	WritableDataBuf writeVarInt(int i);

	/**
	 * Writes the given integer to the buffer as an {@link com.google.common.primitives.UnsignedBytes unsigned byte}
	 *
	 * @param i the integer to writePacketId
	 * @return this
	 */
	WritableDataBuf writeUnsignedByte(int i);

	/**
	 * Writes the given integer to the buffer as an {@link de.take_weiland.mods.commons.util.UnsignedShorts unsigned short}
	 *
	 * @param i the integer to writePacketId
	 * @return this
	 */
	WritableDataBuf writeUnsignedShort(int i);

	/**
	 * writes the given String to the buffer. First a {@link #writeVarInt VarInt} is written containing the length of the string, or -1 if s is null.
	 * Then each character is written to the buffer.
	 *
	 * @param s the String to writePacketId
	 * @return this
	 */
	WritableDataBuf writeString(String s);

	/**
	 * writes the given byte array to the buffer. First, the length (or -1 if null) is written as a {@link #writeVarInt VarInt}, then every byte in the array is written.
	 *
	 * @param bytes the byte array to writePacketId
	 * @return this
	 */
	WritableDataBuf writeBytes(byte[] bytes);

	/**
	 * writes all the bytes in the given byte array to the buffer, without writing the length
	 *
	 * @param bytes the byte array to writePacketId
	 * @return this
	 */
	WritableDataBuf writeRaw(byte[] bytes);

	/**
	 * writes {@code len} bytes from the given byte array to the buffer, starting at {@code off} without writing the length
	 *
	 * @param bytes the byte array to writePacketId
	 * @return this
	 */
	WritableDataBuf writeRaw(byte[] bytes, int off, int len);

	<T> WritableDataBuf write(T obj, ByteStreamSerializer<? super T> serializer);

	WritableDataBuf write(ByteStreamSerializable o);

	/**
	 * grows the internal buffer so that it can hold at least n additional bytes<br>
	 * repeated calls to this method do not accumulate, e.g.:
	 * <pre>{@code
	 * buf.grow(5);
	 * buf.grow(7); }</pre>will only grow the buffer to hold 7 additional bytes, not 12.
	 *
	 * @param n
	 */
	WritableDataBuf grow(int n);

	/**
	 * returns a view of this buffer as an OutputStream. Methods in the returned output stream writePacketId-through to this buffer.
	 *
	 * @return an OutputStream
	 */
	OutputStream asOutputStream();

	/**
	 * <p>returns a view of this buffer as a DataOutput. Methods in the return object writePacketId through to this buffer.</p>
	 * <p>This method is preferred to {@code new DataOutputStream(buf.asOutputStream())}, even though they are currently equivalent</p>
	 *
	 * @return a DataOutput
	 */
	DataOutput asDataOutput();

	byte[] toByteArray();

}
