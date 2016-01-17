package de.take_weiland.mods.commons.internal.worldview;

import de.take_weiland.mods.commons.internal.EntityPlayerMPProxy;
import de.take_weiland.mods.commons.internal.WorldProxy;
import de.take_weiland.mods.commons.util.Players;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;

import java.util.Iterator;

/**
 * @author diesieben07
 */
@SuppressWarnings("ForLoopReplaceableByForEach")
public class ServerChunkViewManager {

    // maps encoded chunks to ChunkInstance objects
    // keeps track of which players watch which chunks
    private static final TLongObjectMap<ChunkInstance> chunkData = new TLongObjectHashMap<>();

    // "public" API, from ClientChunks class

    public static void addView(EntityPlayer player, int dimension, int chunkX, int chunkZ) {
        long chunkEnc = encodeChunk(dimension, chunkX, chunkZ);

        getChunkInstance(chunkEnc).players.add(player);
        ((EntityPlayerMPProxy) player)._sc$viewedChunks().add(chunkEnc);

        WorldServer world = DimensionManager.getWorld(dimension);
        // if world and chunk are loaded and player is not already tracking through vanilla mechanics we need to tell them about the chunk
        if (world != null && ((WorldProxy) world)._sc$chunkExists(chunkX, chunkZ) && !Players.getTrackingChunk(world, chunkX, chunkZ).contains(player)) {
            Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
            PacketChunkData.sendWholeChunk(chunk).sendTo(player);
        }
    }

    public static void removeView(EntityPlayer player, int dimension, int chunkX, int chunkZ) {
        long chunkEnc = encodeChunk(dimension, chunkX, chunkZ);
        if (removePlayer(chunkEnc, player)) {
            ((EntityPlayerMPProxy) player)._sc$viewedChunks().remove(chunkEnc);
            WorldServer world = DimensionManager.getWorld(dimension);
            if (world != null && !Players.getTrackingChunk(world, chunkX, chunkZ).contains(player)) {
                new PacketChunkUnload(dimension, chunkX, chunkZ).sendTo(player);
            }
        }
    }

    // event callbacks

    public static void onPlayerLogout(EntityPlayer player) {
        TLongArrayList chunks = ((EntityPlayerMPProxy) player)._sc$viewedChunks();
        for (int i = 0, len = chunks.size(); i < len; i++) {
            removePlayer(chunks.getQuick(i), player);
        }
    }

    public static void onChunkLoad(Chunk chunk) {
        ChunkInstance chunkInstance = chunkData.get(encodeChunk(chunk));
        if (chunkInstance != null && !chunkInstance.players.isEmpty()) {
            Iterator<EntityPlayer> it = chunkInstance.notAlreadyTrackingIterator(chunk.worldObj, chunk.xPosition, chunk.zPosition);
            if (it.hasNext()) {
                PacketChunkData.sendWholeChunk(chunk).sendTo(it);
            }
        }
    }

    public static void onChunkUnload(Chunk chunk) {
        ChunkInstance chunkInstance = chunkData.get(encodeChunk(chunk));
        if (chunkInstance != null && !chunkInstance.players.isEmpty()) {
            Iterator<EntityPlayer> it = chunkInstance.notAlreadyTrackingIterator(chunk.worldObj, chunk.xPosition, chunk.zPosition);
            if (it.hasNext()) {
                new PacketChunkUnload(chunk.worldObj.provider.dimensionId, chunk.xPosition, chunk.zPosition).sendTo(it);
            }
        }
    }

    // ASM callbacks
    public static final String SUPPRESS_UNLOAD_PACKET = "suppressUnloadPacket";

    public static boolean suppressUnloadPacket(EntityPlayerMP player, Chunk chunk) {
        TLongArrayList viewedChunks = ((EntityPlayerMPProxy) player)._sc$viewedChunks();
        long enc = encodeChunk(chunk);
        boolean contains = viewedChunks.contains(enc);
        if (contains) System.out.println("suppressing unload packet for " + chunk.xPosition + ", " + chunk.zPosition);
        return contains;
    }

    // block change callbacks form ChunkUpdateTracker

    static void onChunkLayersChanged(Chunk chunk, int yLayers) {
        ChunkInstance chunkInstance = chunkData.get(encodeChunk(chunk));
        if (chunkInstance != null && !chunkInstance.players.isEmpty()) {
            Iterator<EntityPlayer> it = chunkInstance.notAlreadyTrackingIterator(chunk.worldObj, chunk.xPosition, chunk.zPosition);
            if (it.hasNext()) {
                PacketChunkData.sendChunkLayers(chunk, yLayers).sendTo(it);
            }
        }
    }

    static void onSingleBlockChanged(Chunk chunk, int x, int y, int z) {
        ChunkInstance chunkInstance = chunkData.get(encodeChunk(chunk));
        if (chunkInstance != null && !chunkInstance.players.isEmpty()) {
            Iterator<EntityPlayer> it = chunkInstance.notAlreadyTrackingIterator(chunk.worldObj, chunk.xPosition, chunk.zPosition);
            if (it.hasNext()) {
                int block = Block.getIdFromBlock(chunk.getBlock(x, y, z));
                int meta = chunk.getBlockMetadata(x, y, z);
                new PacketBlockChange(chunk.worldObj.provider.dimensionId, x + (chunk.xPosition << 4), y, z + (chunk.zPosition << 4), block, meta)
                        .sendTo(it);
            }
        }
    }

    // misc

    // returns true when player was actually there
    private static boolean removePlayer(long enc, EntityPlayer player) {
        ChunkInstance chunkInstance = chunkData.get(enc);
        if (chunkInstance != null && chunkInstance.players.remove(player)) {
            if (chunkInstance.players.isEmpty()) {
                chunkData.remove(enc);
            }
            return true;
        } else {
            return false;
        }
    }

    private static ChunkInstance getChunkInstance(long chunkEnc) {
        ChunkInstance chunk = chunkData.get(chunkEnc);
        if (chunk == null) {
            chunk = new ChunkInstance();
            chunkData.put(chunkEnc, chunk);
        }
        return chunk;
    }

    private static final long DIMENSION_MASK   = 0xF_FFFFL;
    private static final long CHUNK_COORD_MASK = 0x3F_FFFFL;
    private static final long CHUNK_X_SHIFT    = 20;

    private static final long CHUNK_Z_SHIFT = 42;

    private static long encodeChunk(Chunk chunk) {
        return encodeChunk(chunk.worldObj.provider.dimensionId, chunk.xPosition, chunk.zPosition);
    }

    // encode dimension, chunkX and chunkZ into a single long
    private static long encodeChunk(int dimension, int chunkX, int chunkZ) {
        // chunkX and chunkZ need 22 bits each (world allows block coords -30000000-30000000)
        // this assumes that dimensionID doesn't need more than 20 bits. let's just hope so, otherwise revisit this
        // (could for example use an index into a sorted list of used dim IDs instead)
        if (dimension >= 0x7_FFFF || dimension <= 0xFFF8_0000) {
            throw new IllegalStateException("DimensionID out of range, please notify SevenCommons dev.");
        }
        return (dimension & DIMENSION_MASK) | ((chunkX & CHUNK_COORD_MASK) << CHUNK_X_SHIFT) | ((chunkZ & CHUNK_COORD_MASK) << CHUNK_Z_SHIFT);
    }
}
