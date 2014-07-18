package de.take_weiland.mods.commons.net;

import com.google.common.io.ByteArrayDataInput;
import net.minecraft.item.ItemStack;
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

	UUID readUUID();

	<E extends Enum<E>> E readEnum(Class<E> clazz);

	boolean[] readBooleans();

	byte[] readBytes();

	short[] readShorts();

	int[] readInts();

	long[] readLongs();

	char[] readChars();

	float[] readFloats();

	double[] readDoubles();

}
