package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.util.SCReflector;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author diesieben07
 */
class FastBAOS extends OutputStream implements MCDataOuput {

	private byte[] buf;

	private int count;

	FastBAOS() {
		this(64);
	}

	FastBAOS(int initialCap) {
		buf = new byte[initialCap];
	}

	private void ensureCapacity(int minCapacity) {
		if ((count + minCapacity) - buf.length > 0)
			grow(minCapacity + count);
	}

	private void grow(int minCapacity) {
		int oldCapacity = buf.length;
		int newCapacity = oldCapacity << 1;
		if (newCapacity - minCapacity < 0)
			newCapacity = minCapacity;
		if (newCapacity < 0) {
			if (minCapacity < 0)
				throw new OutOfMemoryError();
			newCapacity = Integer.MAX_VALUE;
		}
		buf = Arrays.copyOf(buf, newCapacity);
	}


	@Override
	public void write(int b) {
		ensureCapacity(1);
		buf[count++] = (byte) b;
	}

	@Override
	public void write(@NotNull byte[] b) {
		write(b, 0, b.length);
	}

	@Override
	public void write(@NotNull byte[] b, int off, int len) {
		ensureCapacity(len);
		System.arraycopy(b, off, buf, count, len);
	}

	@Override
	public void flush() { }

	@Override
	public void close() { }

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
		ensureCapacity(2);
		buf[count++] = (byte) (v >> 8);
		buf[count++] = (byte) (v);
	}

	@Override
	public void writeChar(int v) {
		writeShort(v);
	}

	@Override
	public void writeInt(int v) {
		ensureCapacity(4);
		buf[count++] = (byte) (v >> 24);
		buf[count++] = (byte) (v >> 16);
		buf[count++] = (byte) (v >> 8);
		buf[count++] = (byte) (v);

	}

	@Override
	public void writeLong(long v) {
		ensureCapacity(8);
		byte[] buf = this.buf;
		int count = this.count;
		buf[count++] = (byte) (v >> 56);
		buf[count++] = (byte) (v >> 48);
		buf[count++] = (byte) (v >> 40);
		buf[count++] = (byte) (v >> 32);
		buf[count++] = (byte) (v >> 24);
		buf[count++] = (byte) (v >> 16);
		buf[count++] = (byte) (v >> 8);
		buf[count] = (byte) (v);
		this.count = count + 1;
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
	public void writeChars(@NotNull String s) {
		int len = s.length();
		ensureCapacity(len << 1);
		int count = this.count;
		byte[] buf = this.buf;
		for (int i = 0; i < len; ++i) {
			char c = s.charAt(i);
			buf[count++] = (byte) (c >> 8);
			buf[count++] = (byte) (c);
		}
		this.count = count;
	}

	@Override
	public void writeUTF(@NotNull String s) {
		try {
			SCReflector.instance.writeUTF(null, s, this);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void writeBytes(@NotNull String s) {
		int len = s.length();
		ensureCapacity(len);
		byte[] buf = this.buf;
		int count = this.count;
		for (int i = 0; i < len; ++i) {
			buf[count++] = (byte) s.charAt(i);
		}
		this.count = count;
	}

	@Override
	public void writeVarInt(int i) {

	}

	@Override
	public void writeString(String s) {
		writeVarInt(s.length());
		writeChars(s);
	}

	@Override
	public void writeItemStack(ItemStack stack) {
		if (stack == null) {
			writeShort(-1);
		} else {
			writeShort(stack.itemID);
			writeShort(stack.getItemDamage());
			writeByte(stack.stackSize);
			writeNBT(stack.stackTagCompound);
		}
	}

	@Override
	public void writeFluidStack(FluidStack stack) {

	}

	@Override
	public void writeNBT(NBTTagCompound nbt) {
		try {
			CompressedStreamTools.write(nbt, this);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void writeUUID(UUID uuid) {

	}

	@Override
	public <E extends Enum<E>> void writeEnum(E e) {

	}

	@Override
	public void writeBooleans(boolean[] booleans) {

	}

	@Override
	public void writeBytes(byte[] bytes) {

	}

	@Override
	public void writeShorts(short[] shorts) {

	}

	@Override
	public void writeInts(int[] ints) {

	}

	@Override
	public void writeLongs(long[] longs) {

	}

	@Override
	public void writeChars(char[] chars) {

	}

	@Override
	public void writeFloats(float[] floats) {

	}

	@Override
	public void writeDoubles(double[] doubles) {

	}

	@Override
	public byte[] toByteArray() {
		return Arrays.copyOf(buf, count);
	}
}
