package de.take_weiland.mods.commons.util;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;

import com.google.common.base.Preconditions;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.fluids.FluidStack;

public final class MinecraftDataInputImpl implements MinecraftDataInput {

	private final DataInput in;
	
	public static MinecraftDataInput create(byte[] bytes) {
		return new MinecraftDataInputImpl(bytes);
	}
	
	public static MinecraftDataInput create(byte[] bytes, int start) {
		Preconditions.checkPositionIndex(start, bytes.length);
		return new MinecraftDataInputImpl(bytes, start);
	}
	
	private MinecraftDataInputImpl(byte[] bytes) {
		this(new ByteArrayInputStream(bytes));
	}
	
	private MinecraftDataInputImpl(byte[] bytes, int start) {
		this(new ByteArrayInputStream(bytes, start, bytes.length - start));
	}
	
	private MinecraftDataInputImpl(ByteArrayInputStream stream) {
		in = new DataInputStream(stream);
	}
	
	@Override
	public void readFully(byte[] b) {
		try {
			in.readFully(b);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void readFully(byte[] b, int off, int len) {
		try {
			in.readFully(b, off, len);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public int skipBytes(int n) {
		try {
			return in.skipBytes(n);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public boolean readBoolean() {
		try {
			return in.readBoolean();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public byte readByte() {
		try {
			return in.readByte();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public int readUnsignedByte() {
		try {
			return in.readUnsignedByte();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public short readShort() {
		try {
			return in.readShort();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public int readUnsignedShort() {
		try {
			return in.readUnsignedShort();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public char readChar() {
		try {
			return in.readChar();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public int readInt() {
		try {
			return in.readInt();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public long readLong() {
		try {
			return in.readLong();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public float readFloat() {
		try {
			return in.readFloat();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public double readDouble() {
		try {
			return in.readDouble();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String readLine() {
		try {
			return in.readLine();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String readUTF() {
		try {
			return in.readUTF();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public ItemStack readItemStack() {
		try {
			return Packet.readItemStack(in);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public FluidStack readFluidStack() {
		try {
			int fluidId = in.readShort();
			if (fluidId < 0) {
				return null;
			} else {
				int amount = in.readInt();
				NBTTagCompound nbt = readNBTTagCompound();
				return new FluidStack(fluidId, amount, nbt);
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public NBTTagCompound readNBTTagCompound() {
		try {
			int length = in.readShort();
			if (length < 0) {
				return null;
			} else {
				byte[] bytes = new byte[length];
				in.readFully(bytes);
				return CompressedStreamTools.decompress(bytes);
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public <E extends Enum<E>> E readEnum(Class<E> clazz) {
		try {
			return CollectionUtils.safeArrayAccess(clazz.getEnumConstants(), in.readUnsignedByte());
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

}
