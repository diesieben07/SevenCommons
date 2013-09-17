package de.take_weiland.mods.commons.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

import com.google.common.primitives.UnsignedBytes;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraftforge.fluids.FluidStack;

public final class MinecraftDataOutputImpl implements MinecraftDataOutput {

	private final ByteArrayOutputStream stream;
	private final DataOutput out;
	
	public static MinecraftDataOutput create() {
		return new MinecraftDataOutputImpl();
	}
	
	public static MinecraftDataOutput create(int size) {
		return new MinecraftDataOutputImpl(size);
	}
	
	private MinecraftDataOutputImpl() {
		this(new ByteArrayOutputStream());
	}
	
	private MinecraftDataOutputImpl(int size) {
		this(new ByteArrayOutputStream(size));
	}
	
	private MinecraftDataOutputImpl(ByteArrayOutputStream stream) {
		this.stream = stream;
		this.out = new DataOutputStream(stream);
	}

	@Override
	public void write(int b) {
		try {
			out.write(b);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void write(byte[] b) {
		try {
			out.write(b);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void write(byte[] b, int off, int len) {
		try {
			out.write(b, off, len);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void writeBoolean(boolean v) {
		try {
			out.writeBoolean(v);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void writeByte(int v) {
		try {
			out.writeByte(v);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void writeShort(int v) {
		try {
			out.writeShort(v);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void writeChar(int v) {
		try {
			out.writeChar(v);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void writeInt(int v) {
		try {
			out.writeInt(v);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void writeLong(long v) {
		try {
			out.writeLong(v);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void writeFloat(float v) {
		try {
			out.writeFloat(v);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void writeDouble(double v) {
		try {
			out.writeDouble(v);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void writeChars(String s) {
		try {
			out.writeChars(s);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void writeUTF(String s) {
		try {
			out.writeUTF(s);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void writeBytes(String s) {
		try {
			out.writeBytes(s);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void writeItemStack(ItemStack stack) {
		try {
			Packet.writeItemStack(stack, out);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void writeFluidStack(FluidStack stack) {
		try {
			if (stack == null) {
				out.writeShort(-1);
			} else {
				out.writeShort(stack.fluidID);
				out.writeInt(stack.amount);
				writeNBTTagCompound(stack.tag);
			}
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void writeNBTTagCompound(NBTTagCompound nbt) {
		try {
			if (nbt == null) {
				out.writeShort(-1);
			} else {
				byte[] bytes = CompressedStreamTools.compress(nbt);
				out.writeShort(bytes.length);
				out.write(bytes);
			}
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}
	
	@Override
	public void writeEnum(Enum<?> e) {
		try {
			out.writeByte(UnsignedBytes.checkedCast(e.ordinal()));
		} catch (IOException ex) {
			throw new AssertionError(ex);
		}
	}

	@Override
	public byte[] toByteArray() {
		return stream.toByteArray();
	}
	
}
