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

	void writeString(String s);

	void writeItemStack(ItemStack stack);

	void writeFluidStack(FluidStack stack);

	void writeNBT(NBTTagCompound nbt);

	void writeUUID(UUID uuid);

	<E extends Enum<E>> void writeEnum(E e);

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
	byte[] rawBackingArray();

}
