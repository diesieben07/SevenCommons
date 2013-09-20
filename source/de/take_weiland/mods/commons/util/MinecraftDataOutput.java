package de.take_weiland.mods.commons.util;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.fluids.FluidStack;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Shorts;
import com.google.common.primitives.UnsignedBytes;

public final class MinecraftDataOutput extends OutputStream implements DataOutput {

	public static MinecraftDataOutput create() {
		return new MinecraftDataOutput();
	}
	
	public static MinecraftDataOutput create(int size) {
		return new MinecraftDataOutput(size);
	}
	
	private byte[] buffer;
	private int count;
	
	private MinecraftDataOutput() {
		this(32);
	}
	
	private MinecraftDataOutput(int size) {
		checkArgument(size >= 0, "Negative initial size!");
		
		buffer = new byte[size];
	}
	
	private void ensureCapacity(int cap) {
		if (cap - buffer.length > 0) {
			grow(cap);
		}
	}

	private void grow(int cap) {
		int oldCapacity = buffer.length;
		int newCapacity = oldCapacity << 1;
		if (newCapacity - cap < 0)
			newCapacity = cap;
		if (newCapacity < 0) {
			if (cap < 0) // overflow
				throw new OutOfMemoryError();
			newCapacity = Integer.MAX_VALUE;
		}
		buffer = Arrays.copyOf(buffer, newCapacity);
	}

	@Override // OutputStream & DataOutput
	public void write(int b) {
		ensureCapacity(count + 1);
		buffer[count++] = (byte) b;
	}
	
	private void write0(int b) {
		buffer[count++] = (byte) b;
	}

	@Override
	public void write(byte[] b) {
		write(b, 0, b.length);
	}

	@Override
	public void write(byte[] b, int off, int len) {
		if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) - b.length > 0)) {
			throw new IndexOutOfBoundsException();
		}
		
		ensureCapacity(count + len);
		System.arraycopy(b, off, buffer, count, len);
		count += len;
	}
	
	public void skip(int n) {
		checkArgument(n >= 0, "Count must be >= 0");
		if (n > 0) {
			ensureCapacity(count + n);
			count += n;
		}
	}

	@Override
	public void writeBoolean(boolean v) {
		write(v ? 1 : 0);
	}

	@Override
	public void writeByte(int v) {
		write(v);
	}

	@Override
	public void writeShort(int v) {
		ensureCapacity(count + 2);
		write0((v >>> 8) & 0xFF);
		write0(v & 0xFF);
	}

	@Override
	public void writeChar(int v) {
		ensureCapacity(count + 2);
		write0((v >>> 8) & 0xFF);
		write0((v >>> 0) & 0xFF);
	}

	@Override
	public void writeInt(int v) {
		ensureCapacity(count + 4);
		write0((v >>> 24) & 0xFF);
        write0((v >>> 16) & 0xFF);
        write0((v >>>  8) & 0xFF);
        write0((v >>>  0) & 0xFF);
	}

	@Override
	public void writeLong(long v) {
		ensureCapacity(count + 8);
		write0((byte)(v >>> 56));
		write0((byte)(v >>> 48));
		write0((byte)(v >>> 40));
		write0((byte)(v >>> 32));
		write0((byte)(v >>> 24));
		write0((byte)(v >>> 16));
		write0((byte)(v >>>  8));
		write0((byte)(v >>>  0));
	}

	@Override
	public void writeFloat(float v) {
		writeInt(Float.floatToIntBits(v));
	}

	@Override
	public void writeDouble(double v) {
		writeLong(Double.doubleToLongBits(v));
	}

	@Override
	public void writeChars(String s) {
		int len = s.length();
		ensureCapacity(count + len << 1);
		for (int i = 0; i < len; ++i) {
			int c = s.charAt(i);
			write0((c >>> 8) & 0xFF);
			write0((c >>> 0) & 0xFF);
		}
		
	}

	@Override
	public void writeUTF(String s) {
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public void writeBytes(String s) {
		int len = s.length();
		ensureCapacity(count + len);
		for (int i = 0; i < len; ++i) {
			write0(s.charAt(i) & 0xFF);
		}
	}

	private static final int ITEM_STACK_NULL = Shorts.BYTES; // just a -1
	private static final int ITEM_STACK_NONNULL = Shorts.BYTES + 1 + Shorts.BYTES + Shorts.BYTES; // (short)ItemId; (byte)stackSize; (short)damage; (short)NBTSize
	
	public void writeItemStack(ItemStack stack) {
		ensureCapacity(count + (stack == null ? ITEM_STACK_NULL : ITEM_STACK_NONNULL));
		try {
			Packet.writeItemStack(stack, this);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	private static final int FLUID_STACK_NONNULL = Shorts.BYTES + Ints.BYTES + Shorts.BYTES; // (short)fluidId; (int)amount; (short)nbt size
	
	public void writeFluidStack(FluidStack stack) {
		if (stack == null) {
			writeShort(-1);
		} else {
			ensureCapacity(FLUID_STACK_NONNULL);
			writeShort(stack.fluidID);
			writeInt(stack.amount);
			writeNBTTagCompound(stack.tag);
		}
	}

	public void writeNBTTagCompound(NBTTagCompound nbt) {
		writeNBTTagCompound(nbt, true);
	}
	
	public void writeNBTTagCompound(NBTTagCompound nbt, boolean compress) {
		write(nbt == null ? 0 : compress ? 1 : 2);
		if (nbt != null) {
			try {
				if (compress) {
					CompressedStreamTools.writeCompressed(nbt, this);
				} else {
					CompressedStreamTools.write(nbt, this);
				}
			} catch (IOException e) {
				throw new AssertionError();
			}
		}
	}
	
	public void writeEnum(Enum<?> e) {
		write(UnsignedBytes.checkedCast(e.ordinal()));
	}
	
	public void writeJumboEnum(Enum<?> e) {
		writeShort(UnsignedShorts.checkedCast(e.ordinal()));
	}

	public byte[] toNewArray() {
		return Arrays.copyOf(buffer, count);
	}
	
	public byte[] rawArray() {
		return buffer;
	}
	
	public int getCount() {
		return count;
	}
	
}
