package de.take_weiland.mods.commons.net;


import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedBytes;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class DataBuffers {

	private static final int DEFAULT_CAPACTIY = 128;

	private DataBuffers() { }

	/**
	 * <p>returns a new DataBuf that reads from the given array.
	 * Changes to the array are reflected in the buffer</p>
	 * @param data the backing array
	 * @return a new DataBuf
	 */
	public static DataBuf newBuffer(byte[] data) {
		return newBuffer(data, 0, data.length);
	}

	/**
	 * <p>Same as {@link #newBuffer(byte[])}, but only uses {@code len} bytes from the array, starting at {@code off}</p>
	 * @param data the backing array
	 * @param off the offset to start reading from
	 * @param len the number of bytes to read
	 * @return a new DataBuf
	 */
	public static DataBuf newBuffer(byte[] data, int off, int len) {
        validateArray(data, off, len);
		return new DataBufImpl(data, off, len);
	}

	/**
	 * creates a new WritableDataBuf with a starting capacity of 128 bytes
	 * @return a new WritableDataBuf
	 */
	public static WritableDataBuf newWritableBuffer() {
		return newWritable0(DEFAULT_CAPACTIY);
	}

	/**
	 * creates a new WritableDataBuf with the given starting capacity
	 * @return a new WritableDataBuf
	 */
	public static WritableDataBuf newWritableBuffer(int capacity) {
		Preconditions.checkArgument(capacity > 0, "Capacity must be positive");
		return newWritable0(capacity);
	}

	private static DefaultWritableDataBuf newWritable0(int capacity) {
		if (capacity <= 0) {
			capacity = DEFAULT_CAPACTIY;
		}
		return new DefaultWritableDataBuf(new byte[capacity]);
	}

	static byte[] createBuffer(int capacity) {
		return new byte[capacity <= 0 ? DEFAULT_CAPACTIY : capacity];
	}

	static void validateArray(byte[] arr, int off, int len) {
		if (arr == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > arr.length - off) {
			throw new IndexOutOfBoundsException();
		}
	}

	// read-writePacketId utils
	public static void writeEnum(DataOutput out, Enum<?> e) throws IOException {
		out.writeByte(UnsignedBytes.checkedCast(e.ordinal()));
	}

	public static <E extends Enum<E>> E readEnum(DataInput in, Class<E> clazz) throws IOException {
		return JavaUtils.byOrdinal(clazz, in.readUnsignedByte());
	}

	public static void writeNbt(DataOutput out, NBTTagCompound nbt) throws IOException {
		if (nbt == null) {
			out.writeBoolean(true);
		} else {
			out.writeBoolean(false);
			CompressedStreamTools.write(nbt, out);
		}
	}

	public static NBTTagCompound readNbt(DataInput in) throws IOException {
		if (in.readBoolean()) {
			return null;
		} else {
			return CompressedStreamTools.read(in);
		}
	}

	public static void writeFluidStack(DataOutput out, FluidStack stack) throws IOException {
		if (stack == null) {
			out.writeShort(-1);
		} else {
			out.writeShort(stack.fluidID);
			out.writeInt(stack.amount);
			writeNbt(out, stack.tag);
		}
	}

	public static FluidStack readFluidStack(DataInput in) throws IOException {
		short fluidId = in.readShort();
		if (fluidId < 0) {
			return null;
		} else {
			int amount = in.readInt();
			FluidStack stack = new FluidStack(fluidId, amount);
			stack.tag = readNbt(in);
			return stack;
		}
	}

	public static void writeEnum(WritableDataBuf out, Enum<?> e) {
		out.writeVarInt(e.ordinal());
	}

	public static <E extends Enum<E>> E readEnum(DataBuf in, Class<E> clazz) {
		return JavaUtils.byOrdinal(clazz, in.readVarInt());
	}

	public static void writeNbt(WritableDataBuf out, NBTTagCompound nbt) {
		if (nbt == null) {
			out.writeBoolean(true);
		} else {
			out.writeBoolean(false);
			try {
				CompressedStreamTools.write(nbt, out.asDataOutput());
			} catch (IOException e) {
				throw new AssertionError("Impossible");
			}
		}
	}

	public static NBTTagCompound readNbt(DataBuf in) {
		if (in.readBoolean()) {
			return null;
		} else {
			try {
				return CompressedStreamTools.read(in.asDataInput());
			} catch (IOException e) {
				throw new AssertionError("Impossible");
			}
		}
	}

	public static void writeFluidStack(WritableDataBuf out, FluidStack stack) {
		if (stack == null) {
			out.writeVarInt(-1);
		} else {
			out.writeVarInt(stack.fluidID);
			out.writeVarInt(stack.amount);
			writeNbt(out, stack.tag);
		}
	}

	public static FluidStack readFluidStack(DataBuf in) {
		int fluidId = in.readVarInt();
		if (fluidId < 0) {
			return null;
		} else {
			int amount = in.readVarInt();
			FluidStack stack = new FluidStack(fluidId, amount);
			stack.tag = readNbt(in);
			return stack;
		}
	}

	public static void writeItemStack(WritableDataBuf out, ItemStack stack) {
		if (stack == null) {
			out.writeShort(-1);
		} else {
			out.writeShort(stack.itemID);
			out.writeByte(stack.stackSize);
			out.writeShort(stack.getItemDamage());
			writeNbt(out, stack.stackTagCompound);
		}
	}

	public static ItemStack readItemStack(DataBuf in) {
		int id = in.readShort();
		if (id < 0) {
			return null;
		} else {
			int size = in.readByte();
			int damage = in.readShort();
			ItemStack stack = new ItemStack(id, size, damage);
			stack.stackTagCompound = readNbt(in);
			return stack;
		}
	}

}
