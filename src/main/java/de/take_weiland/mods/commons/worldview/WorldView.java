package de.take_weiland.mods.commons.worldview;

import net.minecraft.world.ChunkCoordIntPair;

import java.util.Set;

/**
 * @author diesieben07
 */
public interface WorldView {

    int id();

    int dimensionID();

    Set<ChunkCoordIntPair> viewedChunks();

    boolean containsChunk(int chunkX, int chunkZ);

}
