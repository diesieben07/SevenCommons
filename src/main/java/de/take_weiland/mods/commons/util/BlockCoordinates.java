package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.nbt.NBTSerializable;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * @author diesieben07
 */
public final class BlockCoordinates implements ByteStreamSerializable, NBTSerializable, Comparable<BlockCoordinates> {

	private final int x;
	private final int y;
	private final int z;

	public BlockCoordinates(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public BlockCoordinates(BlockCoordinates other) {
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

	public float distanceTo(BlockCoordinates coords) {
		return distanceTo(coords.x, coords.y, coords.z);
	}

	public float distanceSq(int x, int y, int z) {
		float dX = this.x - x;
		float dY = this.y - y;
		float dZ = this.z - z;
		return dX * dX + dY * dY + dZ * dZ;
	}

	public float distanceSq(BlockCoordinates coords) {
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
	public void writeTo(MCDataOutputStream out) {
		out.writeInt(x);
		out.writeByte(y);
		out.writeInt(z);
	}

	@Override
	public NBTBase serialize() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("x", x);
		nbt.setByte("y", (byte) y);
		nbt.setInteger("z", z);
		return nbt;
	}

	@Override
	public int compareTo(BlockCoordinates o) {
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
		if (o instanceof BlockCoordinates) {
			BlockCoordinates other = (BlockCoordinates) o;
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

	public static void toByteStream(MCDataOutputStream out, int x, int y, int z) {
		out.writeInt(x);
		out.writeByte(y);
		out.writeInt(z);
	}

	// pseudo-constructors
	@ByteStreamSerializable.Deserializer
	public static BlockCoordinates fromByteStream(MCDataInputStream in) {
		int x = in.readInt();
		int y = in.readUnsignedByte();
		int z = in.readInt();
		return new BlockCoordinates(x, y, z);
	}

	@NBTSerializable.Deserializer
	public static BlockCoordinates fromNBT(NBTBase nbt) {
		NBTTagCompound compound = (NBTTagCompound) nbt;
		int x = compound.getInteger("x");
		int y = compound.getByte("y") & 0xFF;
		int z = compound.getInteger("z");
		return new BlockCoordinates(x, y, z);
	}

}
