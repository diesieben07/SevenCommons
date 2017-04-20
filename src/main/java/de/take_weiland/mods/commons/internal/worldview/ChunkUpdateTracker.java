package de.take_weiland.mods.commons.internal.worldview;

import de.take_weiland.mods.commons.internal.ChunkProxy;
import de.take_weiland.mods.commons.internal.WorldProxy;
import de.take_weiland.mods.commons.internal.WorldServerProxy;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * @author diesieben07
 */
public class ChunkUpdateTracker {

    // if this flag is present in the data, the data represents a bitmap of changed layers instead of one block pos
    private static final int FLAG_LAYERS        = 0x8000_0000;
    // if this flag is present in the data, the data represents a single changed block with x | (y << 4) | (z << 12)
    private static final int FLAG_BLOCK_PRESENT = 0x4000_0000;
    // mask to only leave the layer bitmask when FLAG_LAYERS is present
    private static final int MASK_CHUNK_LAYERS  = 0xFFFF;

    public static void postWorldTick(World world) {
        long[] changedChunks = ((WorldServerProxy) world)._sc$getChangedChunks();
        if (changedChunks != null) {
            for (int i = 1, limit = (int) changedChunks[0]; i < limit; i++) {
                long l = changedChunks[i];
                int chunkX = (int) l;
                int chunkZ = (int) (l >>> 32L);
                if (((WorldProxy) world)._sc$chunkExists(chunkX, chunkZ)) {
                    Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
                    int chunkData = ((ChunkProxy) chunk)._sc$getBlockUpdates();

                    if ((chunkData & FLAG_LAYERS) != 0) {
                        ServerChunkViewManager.onChunkLayersChanged(chunk, chunkData & MASK_CHUNK_LAYERS);
                    } else if ((chunkData & FLAG_BLOCK_PRESENT) != 0) {
                        int blockX = chunkData & 0xF;
                        int blockY = chunkData >>> 4 & 0xFF;
                        int blockZ = chunkData >>> 12 & 0xF;
                        ServerChunkViewManager.onSingleBlockChanged(chunk, blockX, blockY, blockZ);
                    } else {
                        throw new IllegalStateException("Chunk was marked as needing updates in world but didn't actually have any!?");
                    }

                    ((ChunkProxy) chunk)._sc$setBlockUpdates(0);
                }
            }

            changedChunks[0] = 1L;
        }
    }

    /**
     * <p>Marks given block in the chunk as needing update. x and z must be 0-15, y 0-255.</p>
     *
     * @param chunk chunk
     * @param x     x coord
     * @param y     y coord
     * @param z     z coord
     */
    public static void onChunkBlockUpdate(Chunk chunk, int x, int y, int z) {
        int data = ((ChunkProxy) chunk)._sc$getBlockUpdates();

        if (data == 0) {
            WorldServerProxy proxy = (WorldServerProxy) chunk.getWorld();
            long[] arr = proxy._sc$getChangedChunks();
            arr = appendChunkMark(arr, ChunkPos.asLong(chunk.xPosition, chunk.zPosition));
            proxy._sc$setChangedChunks(arr);
        }

        if ((data & FLAG_LAYERS) != 0) {
            data |= 1 << (y >>> 4);
        } else if ((data & FLAG_BLOCK_PRESENT) != 0) {
            data = FLAG_LAYERS | (data >>> 4 & 0xFF) >>> 4 | y >>> 4;
        } else {
            data = FLAG_BLOCK_PRESENT | x | y << 4 | z << 12;
        }

        ((ChunkProxy) chunk)._sc$setBlockUpdates(data);
    }

    private static long[] appendChunkMark(@Nullable long[] arr, long val) {
        if (arr == null) {
            arr = new long[16];
            arr[0] = 2L;
            arr[1] = val;
        } else {
            int limit = (int) arr[0];
            if (limit == arr.length) {
                arr = Arrays.copyOf(arr, arr.length << 1);
            }
            arr[0] = limit + 1;
            arr[limit] = val;
        }

        return arr;
    }

}
