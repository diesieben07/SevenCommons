package de.take_weiland.mods.commons.util;

import com.google.common.primitives.UnsignedBytes;
import de.take_weiland.mods.commons.nbt.NBTSerializer;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class BlockPos implements Comparable<BlockPos> {

	final int x;
	final int y;
	final int z;

	public BlockPos(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public BlockPos(BlockPos other) {
		this(other.x, other.y, other.z);
	}

	public int x() {
		return x;
	}

	public int y() {
		return y;
	}

	public int z() {
		return z;
	}

	public Block getBlock(World world) {
		return Block.blocksList[world.getBlockId(x, y, z)];
	}

	public int getMetadata(World world) {
		return world.getBlockMetadata(x, y, z);
	}

	public TileEntity getTileEntity(World world) {
		return world.getBlockTileEntity(x, y, z);
	}

	public float distanceTo(int x, int y, int z) {
		float dX = this.x - x;
		float dY = this.y - y;
		float dZ = this.z - z;
		return (float) Math.sqrt(dX * dX + dY * dY + dZ * dZ);
	}

	public float distanceTo(BlockPos coords) {
		return distanceTo(coords.x, coords.y, coords.z);
	}

	public float distanceSq(int x, int y, int z) {
		float dX = this.x - x;
		float dY = this.y - y;
		float dZ = this.z - z;
		return dX * dX + dY * dY + dZ * dZ;
	}

	public float distanceSq(BlockPos coords) {
		return distanceSq(coords.x, coords.y, coords.z);
	}

	public boolean valid() {
		return x >= -30000000 && x < 30000000 && z >= -30000000 && z < 30000000 && y >= 0 && y < 256;
	}

	public void validate() {
		if (!valid()) {
			throw new IllegalStateException(String.format("Illegal Block coordinates [%d %d %d]", x, y, z));
		}
	}

	@Override
	public int compareTo(BlockPos o) {
		int myY;
		int oY;
		if ((myY = y) == (oY = o.y)) {
			int myZ;
			int oZ;
			if ((myZ = z) == (oZ = o.z)) {
				return x - o.x;
			} else {
				return myZ - oZ;
			}
		} else {
			return myY - oY;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof BlockPos) {
			BlockPos other = (BlockPos) o;
			return this.x == other.x && this.y == other.y && this.z == other.z;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return hash(x, y, z);
	}

	public static int hash(int x, int y, int z) {
		// shift z to avoid hash == y for x == z
		return x ^ y ^ (z << 6);
	}

	public static void toByteStream(MCDataOutput out, int x, int y, int z) {
		out.writeInt(x);
		out.writeByte(y);
		out.writeInt(z);
	}

	public static ByteStreamSerializer<BlockPos> streamSerializer() {
		return streamSerializer;
	}

	public static NBTSerializer<BlockPos> nbtSerializer() {
		return nbtSerializer;
	}

	private static final ByteStreamSerializer<BlockPos> streamSerializer = new ByteStreamSerializer<BlockPos>() {
		@Override
		public void write(BlockPos instance, MCDataOutputStream out) {
			out.writeInt(instance.x);
			out.writeByte(instance.y);
			out.writeInt(instance.z);
		}

		@Override
		public BlockPos read(MCDataInputStream in) {
			return new BlockPos(in.readInt(), in.readUnsignedByte(), in.readInt());
		}
	};

	private static final NBTSerializer<BlockPos> nbtSerializer = new NBTSerializer<BlockPos>() {
		@Override
		public NBTBase serialize(BlockPos instance) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setInteger("x", instance.x);
			nbt.setByte("y", (byte) instance.y);
			nbt.setInteger("z", instance.z);
			return nbt;
		}

		@Override
		public BlockPos deserialize(NBTBase nbt) {
			NBTTagCompound comp = (NBTTagCompound) nbt;
			return new BlockPos(comp.getInteger("x"), UnsignedBytes.toInt(comp.getByte("y")), comp.getInteger("z"));
		}
	};

}
