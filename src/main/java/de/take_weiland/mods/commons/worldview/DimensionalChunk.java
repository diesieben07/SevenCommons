package de.take_weiland.mods.commons.worldview;

/**
 * <p>Chunk coordinates combined with a dimension ID.</p>
 *
 * @author diesieben07
 */
public final class DimensionalChunk {

    private final int dimension, x, z;

    /**
     * <p>Create a new DimensionalChunk.</p>
     *
     * @param dimension the dimensionID
     * @param x         chunk x coordinate
     * @param z         chunk z coordinate
     */
    public DimensionalChunk(int dimension, int x, int z) {
        this.dimension = dimension;
        this.x = x;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public int getDimension() {
        return dimension;
    }
}
