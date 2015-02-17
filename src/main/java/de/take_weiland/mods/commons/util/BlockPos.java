package de.take_weiland.mods.commons.util;

import com.google.common.base.Objects;
import de.take_weiland.mods.commons.SerializationMethod;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.serialize.NBTSerializer;
import de.take_weiland.mods.commons.sync.Property;
import de.take_weiland.mods.commons.sync.SyncableProperty;
import de.take_weiland.mods.commons.sync.Watcher;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

	public long toLong() {
		return toLong(x, y, z);
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

	@Override
	public String toString() {
		return "BlockPos(" + x + ", " + y + ", " + z + ")";
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

	private static final int X_Z_BITS = 26;
	private static final long X_Z_MASK = (1L << X_Z_BITS) - 1L;

	private static final long Y_BITS = 8L;
	private static final long Y_MASK = (1L << Y_BITS) - 1L;
	@SuppressWarnings("SuspiciousNameCombination")
	private static final long Y_SHIFT = X_Z_BITS;

	private static final long Z_SHIFT = Y_SHIFT + Y_BITS;

	public static long toLong(int x, int y, int z) {
		return x & X_Z_MASK
				| (y & Y_MASK) << Y_SHIFT
				| (z & X_Z_MASK) << Z_SHIFT;
	}

	private static final long X_RV_MASK = (1L << X_Z_BITS) - 1;
	private static final long Y_RV_MASK = ((1L << Y_BITS) - 1) << Y_SHIFT;
	private static final long Z_RV_MASK = ((1L << X_Z_BITS) - 1) << Z_SHIFT;
	private static final long X_SIGN_BIT = 1 << (X_Z_BITS - 1);
	private static final long Z_SIGN_BIT = 1L << (X_Z_BITS + X_Z_BITS + Y_BITS - 1);

	private static final long NULL_LONG_VAL = 1L << 63;

	public static BlockPos fromLong(long l) {
		return new BlockPos((int) ((l & X_RV_MASK) | -(l & X_SIGN_BIT)),
				(int) ((l & Y_RV_MASK) >>> 26),
				(int) (((l & Z_RV_MASK) | -(l & Z_SIGN_BIT)) >> 34));
	}

	private enum ValueHandler implements NBTSerializer<BlockPos>, Watcher<BlockPos> {

		@NBTSerializer.Provider(forType = BlockPos.class, method = SerializationMethod.Method.VALUE)
		@Watcher.Provider(forType = BlockPos.class, method = SerializationMethod.Method.VALUE)
		INSTANCE;

		// NBTSerializer

		@Nonnull
		@Override
		public <OBJ> NBTBase serialize(Property<BlockPos, OBJ> property, OBJ instance) {
			BlockPos pos = property.get(instance);

			NBTTagList nbt = new NBTTagList();
			nbt.appendTag(new NBTTagInt("", pos.x));
			nbt.appendTag(new NBTTagInt("", pos.y));
			nbt.appendTag(new NBTTagInt("", pos.z));

			return nbt;
		}

		@Override
		public <OBJ> void deserialize(NBTBase nbt, Property<BlockPos, OBJ> property, OBJ instance) {
			NBTTagList list = (NBTTagList) nbt;
			BlockPos pos = new BlockPos(
					((NBTTagInt) list.tagAt(0)).data,
					((NBTTagInt) list.tagAt(1)).data,
					((NBTTagInt) list.tagAt(2)).data
			);
			property.set(pos, instance);
		}

		// Watcher

		@Override
		public <OBJ> void setup(SyncableProperty<BlockPos, OBJ> property, OBJ instance) { }

		@Override
		public <OBJ> void initialWrite(MCDataOutput out, SyncableProperty<BlockPos, OBJ> property, OBJ instance) {
			write(out, property.get(instance));
		}

		private static void write(MCDataOutput out, @Nullable BlockPos blockPos) {
			out.writeLong(blockPos == null ? NULL_LONG_VAL : blockPos.toLong());
		}

		@Override
		public <OBJ> boolean hasChanged(SyncableProperty<BlockPos, OBJ> property, OBJ instance) {
			return !Objects.equal(property.get(instance), property.getData(instance));
		}

		@Override
		public <OBJ> void writeAndUpdate(MCDataOutput out, SyncableProperty<BlockPos, OBJ> property, OBJ instance) {
			BlockPos pos = property.get(instance);
			write(out, pos);
			property.setData(pos, instance);
		}

		@Override
		public <OBJ> void read(MCDataInput in, SyncableProperty<BlockPos, OBJ> property, OBJ instance) {
			long l = in.readLong();
			property.set(l == NULL_LONG_VAL ? null : fromLong(l), instance);
		}
	}

}
