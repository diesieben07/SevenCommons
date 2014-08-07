package de.take_weiland.mods.commons.net;

import com.google.common.io.ByteArrayDataOutput;
import de.take_weiland.mods.commons.Unsafe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.BitSet;
import java.util.UUID;

/**
 * @author diesieben07
 */
public interface MCDataOuput extends ByteArrayDataOutput {

	/**
	 * <p>Write the given String to this stream.</p>
	 * <p>If the String is null, this method writes a VarInt -1, otherwise it first writes the length of the String
	 * as a VarInt and then every character in the String, as specified by {@link #writeChar(int)}.</p>
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
	 *     <li>The NBT tag of the ItemStack, via the {@link #writeNBT(net.minecraft.nbt.NBTTagCompound)} method</li>
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
	 *     <li>The NBT tag of the FluidStack, via the {@link #writeNBT(net.minecraft.nbt.NBTTagCompound)} method</li>
	 * </ol>
	 * @param stack the FluidStack to write, may be null
	 */
	void writeFluidStack(FluidStack stack);

	/**
	 * <p>Write the given NBTTagCompound to this stream.</p>
	 * <p>If the NBTTagCompound is null, this method writes a byte -1. Otherwise it writes every tag in the NBTTagCompound
	 * via the {@link net.minecraft.nbt.NBTBase#writeNamedTag(net.minecraft.nbt.NBTBase, java.io.DataOutput)} method and
	 * terminates with a byte 0.</p>
	 *
	 * @param nbt the NBTTagCompound to write
	 */
	void writeNBT(NBTTagCompound nbt);

	/**
	 * <p>Write the given UUID to this stream.</p>
	 * <p>If the given UUID is null, a short 0xF000 is written (a fake UUID version which does not exist). Otherwise two
	 * long values are written, first the most significant bits of the UUID and then the least significant bits.</p>
	 * @param uuid the UUID to write
	 */
	void writeUUID(UUID uuid);

	/**
	 * <p>Write the given Enum to this stream.</p>
	 * <p>If the given Enum is null, a VarInt -1 is written, otherwise the {@link Enum#ordinal() ordinal} value of the Enum
	 * is written as a VarInt.</p>
	 * @param e the Enum to write
	 */
	<E extends Enum<E>> void writeEnum(E e);

	/**
	 * <p>Writes a long </p>
	 * @param bitSet
	 */
	void writeBitSet(BitSet bitSet);

	void writeVarInt(int i);

	void writeBooleans(boolean[] booleans);

	void writeBooleans(boolean[] booleans, int off, int len);

	void writeBytes(byte[] bytes);

	void writeBytes(byte[] bytes, int off, int len);

	void writeShorts(short[] shorts);

	void writeShorts(short[] shorts, int off, int len);

	void writeInts(int[] ints);

	void writeInts(int[] ints, int off, int len);

	void writeLongs(long[] longs);

	void writeLongs(long[] longs, int off, int len);

	void writeChars(char[] chars);

	void writeChars(char[] chars, int off, int len);

	void writeFloats(float[] floats);

	void writeFloats(float[] floats, int off, int len);

	void writeDoubles(double[] doubles);

	void writeDoubles(double[] doubles, int off, int len);

	void writeTo(OutputStream stream) throws IOException;

	void writeTo(DataOutput out) throws IOException;

	int length();

	@Unsafe
	byte[] backingArray();

	void lock();

	boolean isLocked();

}
