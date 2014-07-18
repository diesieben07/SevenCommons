package de.take_weiland.mods.commons.net;

import com.google.common.io.ByteArrayDataOutput;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

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

	void writeVarInt(int i);

	void writeBooleans(boolean[] booleans);

	void writeBytes(byte[] bytes);

	void writeShorts(short[] shorts);

	void writeInts(int[] ints);

	void writeLongs(long[] longs);

	void writeChars(char[] chars);

	void writeFloats(float[] floats);

	void writeDoubles(double[] doubles);

}
