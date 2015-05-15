package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.net.MCDataOutput;
import net.minecraft.block.Block;
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
        return world.getBlock(x, y, z);
    }

    public int getMetadata(World world) {
        return world.getBlockMetadata(x, y, z);
    }

    public TileEntity getTileEntity(World world) {
        return world.getTileEntity(x, y, z);
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

}
