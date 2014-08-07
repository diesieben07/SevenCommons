package de.take_weiland.mods.commons.net;

import com.google.common.io.ByteArrayDataOutput;
import de.take_weiland.mods.commons.Unsafe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.BitSet;
import java.util.UUID;

/**
 * <p>An extension to the DataOutput interface which adds methods useful in the Minecraft environment.</p>
 * <p>This interface is intended to write to a memory-based buffer and thus does not throw IOExceptions.</p>
 * <p>This interface is mostly intended for network communication purposes and <i>not</i> for saving data to disk.
 * Additionally this interface specifies <b>Little Endian</b> byte order, violating the contract of the DataOutput interface,
 * to offer better performance on most systems.</p>
 * <p>An implementation is offered by {@link de.take_weiland.mods.commons.net.MCDataOutputStream}.</p>
 *
 * @author diesieben07
 */
public interface MCDataOuput extends ByteArrayDataOutput {

	/**
	 * <p>Write the given String to this stream.</p>
	 * <p>If the String is null, this method writes a VarInt -1, otherwise it first writes the length of the String
	 * as a VarInt and then every character in the String, as if by the {@link #writeChar(int)} method.</p>
	 * @param s the String to write, may be null
	 */
	void writeString(String s);

	/**
	 * <p>Write the given ItemStack to this stream.</p>
	 * <p>If the ItemStack is null, this method writes a short -1, otherwise it writes the data of the ItemStack in
	 * the following manner:</p>
	 * <ol>
	 *     <li>The ItemID of the ItemStack as a short</li>
	 *     <li>The damage value of the ItemStack as a short</li>
	 *     <li>The stack size of the ItemStack as a byte</li>
	 *     <li>The NBT tag of the ItemStack, as if by the {@link #writeNBT(net.minecraft.nbt.NBTTagCompound)} method</li>
	 * </ol>
	 * @param stack the ItemStack to write, may be null
	 */
	void writeItemStack(ItemStack stack);

	/**
	 * <p>Write the given FluidStack to this stream.</p>
	 * <p>If the FluidStack is null, this method writes a VarInt -1, otherwise it writes the data of the FluidStack in
	 * the following manner:</p>
	 * <ol>
	 *     <li>The FluidID of the FluidStack as a VarInt</li>
	 *     <li>The amount of the FluidStack as a VarInt</li>
	 *     <li>The NBT tag of the FluidStack, as if by the {@link #writeNBT(net.minecraft.nbt.NBTTagCompound)} method</li>
	 * </ol>
	 * @param stack the FluidStack to write, may be null
	 */
	void writeFluidStack(FluidStack stack);

	/**
	 * <p>Write the given NBTTagCompound to this stream.</p>
	 * <p>If the NBTTagCompound is null, this method writes a byte -1. Otherwise it writes the following for every tag
	 * in the compound and terminates with a byte 0:</p>
	 * <ol>
	 *     <li>The {@link net.minecraft.nbt.NBTBase#getId() Type ID} of the tag as a byte</li>
	 *     <li>The name of the tag, as if by the {@link #writeString(String)} method</li>
	 *     <li>The data of the tag, via the {@link net.minecraft.nbt.NBTBase#write(java.io.DataOutput)} method</li>
	 * </ol>
	 *
	 * @param nbt the NBTTagCompound to write, may be null
	 */
	void writeNBT(NBTTagCompound nbt);

	/**
	 * <p>Write the given UUID to this stream.</p>
	 * <p>If the given UUID is null, a short 0xF000 is written (a fake UUID version which does not exist). Otherwise two
	 * long values are written, first the most significant bits of the UUID and then the least significant bits.</p>
	 * @param uuid the UUID to write, may be null
	 */
	void writeUUID(UUID uuid);

	/**
	 * <p>Write the given Enum to this stream.</p>
	 * <p>If the given Enum is null, a VarInt -1 is written, otherwise the {@link Enum#ordinal() ordinal} value of the Enum
	 * is written as a VarInt.</p>
	 * @param e the Enum to write, may be null
	 */
	<E extends Enum<E>> void writeEnum(E e);

	/**
	 * <p>Write the given BitSet to this stream.</p>
	 * <p>If the BitSet is null, a null long array is written as if by the {@link #writeLongs(long[])} method. Otherwise a
	 * long array equivalent to the result of {@link java.util.BitSet#toLongArray()} is written in the same manner.</p>
	 * @param bitSet the BitSet to write, may be null
	 */
	void writeBitSet(BitSet bitSet);

	/**
	 * <p>Write a VarInt to the stream.</p>
	 * <p>The method writes the given int as a series of 7-bit chunks in little endian order.
	 * Every chunk is written as the least significant bits of a byte, with the most significant bit set if another chunk
	 * follows.</p>
	 * @param i the int to write
	 */
	void writeVarInt(int i);

	/**
	 * <p>Write the given boolean array to the stream.</p>
	 * <p>Ìf the array is null, this method writes a VarInt -1. Otherwise this method first writes the length of the array
	 * as a VarInt and then the booleans as a series of {@code booleans.length %8 + 1} bytes. 8 elements are written as
	 * one byte, each bit in the byte representing one element, least significant bits first.
	 * True is represented by a set bit, false by an unset bit.</p>
	 * @param booleans the array to write
	 */
	void writeBooleans(boolean[] booleans);

	/**
	 * <p>Write the specified portion of the given boolean array to the stream.</p>
	 * <p>This method writes the booleans in the same manner as the {@link #writeBooleans(boolean[])} method.</p>
	 * @param booleans the array to write
	 * @param off the offset of the first element to write
	 * @param len the number of elements to write
	 */
	void writeBooleans(boolean[] booleans, int off, int len);

	/**
	 * <p>Write the given byte array to the stream.</p>
	 * <p>If the array is null, this method writes a VarInt -1. Otherwise this method first writes the length of the array
	 * as a VarInt and then each byte as if by the {@link #writeByte(int)} method.</p>
	 * @param bytes the array to write
	 */
	void writeBytes(byte[] bytes);

	/**
	 * <p>Write the specified portion of the given byte array to the stream.</p>
	 * <p>This method writes the bytes in the same manner as the {@link #writeBytes(byte[])} method.</p>
	 * @param bytes the array to write
	 * @param off the offset of the first element to write
	 * @param len the number of elements to write
	 */
	void writeBytes(byte[] bytes, int off, int len);

	/**
	 * <p>Write the given short array to the stream.</p>
	 * <p>If the array is null, this method writes a VarInt -1. Otherwise this method first writes the length of the array
	 * as a VarInt and then each short as if by the {@link #writeShort(int)} method.</p>
	 * @param shorts the array to write
	 */
	void writeShorts(short[] shorts);

	/**
	 * <p>Write the specified portion of the given short array to the stream.</p>
	 * <p>This method writes the shorts in the same manner as the {@link #writeShorts(short[])} method.</p>
	 * @param shorts the array to write
	 * @param off the offset of the first element to write
	 * @param len the number of elements to write
	 */
	void writeShorts(short[] shorts, int off, int len);

	/**
	 * <p>Write the given int array to the stream.</p>
	 * <p>If the array is null, this method writes a VarInt -1. Otherwise this method first writes the length of the array
	 * as a VarInt and then each int as if by the {@link #writeInt(int)} method.</p>
	 * @param ints the array to write
	 */
	void writeInts(int[] ints);

	/**
	 * <p>Write the specified portion of the given int array to the stream.</p>
	 * <p>This method writes the ints in the same manner as the {@link #writeInts(int[])} method.</p>
	 * @param ints the array to write
	 * @param off the offset of the first element to write
	 * @param len the number of elements to write
	 */
	void writeInts(int[] ints, int off, int len);

	/**
	 * <p>Write the given long array to the stream.</p>
	 * <p>If the array is null, this method writes a VarInt -1. Otherwise this method first writes the length of the array
	 * as a VarInt and then each long as if by the {@link #writeLong(long)} method.</p>
	 * @param bytes the array to write
	 */
	void writeLongs(long[] longs);

	/**
	 * <p>Write the specified portion of the given long array to the stream.</p>
	 * <p>This method writes the longs in the same manner as the {@link #writeLongs(long[])} method.</p>
	 * @param booleans the array to write
	 * @param off the offset of the first element to write
	 * @param len the number of elements to write
	 */
	void writeLongs(long[] longs, int off, int len);

	/**
	 * <p>Write the given char array to the stream.</p>
	 * <p>If the array is null, this method writes a VarInt -1. Otherwise this method first writes the length of the array
	 * as a VarInt and then each char as if by the {@link #writeChar(int)} method.</p>
	 * @param bytes the array to write
	 */
	void writeChars(char[] chars);

	/**
	 * <p>Write the specified portion of the given char array to the stream.</p>
	 * <p>This method writes the chars in the same manner as the {@link #writeChars(char[])} method.</p>
	 * @param chars the array to write
	 * @param off the offset of the first element to write
	 * @param len the number of elements to write
	 */
	void writeChars(char[] chars, int off, int len);

	/**
	 * <p>Write the given float array to the stream.</p>
	 * <p>If the array is null, this method writes a VarInt -1. Otherwise this method first writes the length of the array
	 * as a VarInt and then each float as if by the {@link #writeFloat(float)} method.</p>
	 * @param floats the array to write
	 */
	void writeFloats(float[] floats);

	/**
	 * <p>Write the specified portion of the given float array to the stream.</p>
	 * <p>This method writes the floats in the same manner as the {@link #writeFloats(float[])} method.</p>
	 * @param floats the array to write
	 * @param off the offset of the first element to write
	 * @param len the number of elements to write
	 */
	void writeFloats(float[] floats, int off, int len);

	/**
	 * <p>Write the given double array to the stream.</p>
	 * <p>If the array is null, this method writes a VarInt -1. Otherwise this method first writes the length of the array
	 * as a VarInt and then each double as if by the {@link #writeDouble(double)} method.</p>
	 * @param doubles the array to write
	 */
	void writeDoubles(double[] doubles);

	/**
	 * <p>Write the specified portion of the given double array to the stream.</p>
	 * <p>This method writes the doubles in the same manner as the {@link #writeDoubles(double[])} method.</p>
	 * @param doubles the array to write
	 * @param off the offset of the first element to write
	 * @param len the number of elements to write
	 */
	void writeDoubles(double[] doubles, int off, int len);

	/**
	 * <p>Write the contents of this stream to the given OutputStream as a series of bytes.</p>
	 * @param stream the OutputStream to write to
	 */
	void writeTo(OutputStream stream) throws IOException;

	/**
	 * <p>Write the contents of this stream to the given DataOutput as a series of bytes.</p>
	 * @param out the DataOutput to write to
	 */
	void writeTo(DataOutput out) throws IOException;

	/**
	 * <p>Write the contents of this stream to the given ByteBuffer.</p>
	 * @param buf the buffer to write to
	 */
	void writeTo(ByteBuffer buf);

	/**
	 * <p>Write the contents of this stream to the given ByteChannel.</p>
	 * @param channel the channel to write to
	 */
	void writeTo(WritableByteChannel channel) throws IOException;

	/**
	 * <p>The number of bytes written to this stream to far.</p>
	 * @return the number of bytes
	 */
	int length();

	/**
	 * <p>Return the array backing this stream. Any modification in the returned array will be reflected in the contents
	 * of this stream, as long as the buffer does not grow.</p>
	 * @return the array backing this buffer
	 */
	@Unsafe
	byte[] backingArray();

	/**
	 * <p>Lock this stream so that no further write operations can occur. Attempts to write to a locked stream will
	 * result in an {@link java.lang.IllegalStateException}.</p>
	 */
	void lock();

	/**
	 * <p>Check if this buffer is currently locked.</p>
	 * @return if this buffer is locked
	 * @see #lock()
	 */
	boolean isLocked();

}
