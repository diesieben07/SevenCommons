package de.take_weiland.mods.commons.net;

import com.google.common.io.ByteArrayDataInput;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import java.util.UUID;

/**
 * @author diesieben07
 */
public interface MCDataInput extends ByteArrayDataInput {

	int readVarInt();

	String readString();

	ItemStack readItemStack();

	FluidStack readFluidStack();

	NBTTagCompound readNbt();

	UUID readUUID();

	<E extends Enum<E>> E readEnum(Class<E> clazz);

	boolean[] readBooleans();
	boolean[] readBooleans(boolean[] buf);

	byte[] readBytes();
	byte[] readBytes(byte[] buf);

	short[] readShorts();
	short[] readShorts(short[] buf);

	int[] readInts();
	int[] readInts(int[] buf);

	long[] readLongs();
	long[] readLongs(long[] buf);

	char[] readChars();
	char[] readChars(char[] buf);

	float[] readFloats();
	float[] readFloats(float[] buf);

	double[] readDoubles();
	double[] readDoubles(double[] buf);

}
