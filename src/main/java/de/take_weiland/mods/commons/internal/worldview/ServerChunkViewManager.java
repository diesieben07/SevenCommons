package de.take_weiland.mods.commons.internal.worldview;

import de.take_weiland.mods.commons.internal.EntityPlayerMPProxy;
import de.take_weiland.mods.commons.internal.WorldProxy;
import de.take_weiland.mods.commons.net.simple.SimplePacket;
import de.take_weiland.mods.commons.util.Players;
import de.take_weiland.mods.commons.worldview.DimensionalChunk;
import gnu.trove.impl.hash.TPrimitiveHash;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.hash.TLongHashSet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;

import java.util.Iterator;
import java.util.Set;

/**
 * @author diesieben07
 */
@SuppressWarnings("ForLoopReplaceableByForEach")
public class ServerChunkViewManager {

    // maps encoded chunks to ChunkInstance objects
    // keeps track of which players watch which chunks
    private static final TLongObjectHashMap<ChunkInstance> chunkData = new TLongObjectHashMap<>();

    private static final int INIT_NEW = -1;

    // "public" API, from ClientChunks class

    // add new chunk view for given player if not already present
    public static void addView(EntityPlayer player, int dimension, int chunkX, int chunkZ) {
        long chunkEnc = encodeChunk(dimension, chunkX, chunkZ);

        if (getChunkInstance(chunkEnc).players.add(player)) {
            ((EntityPlayerMPProxy) player)._sc$viewedChunks().add(chunkEnc);

            WorldServer world = DimensionManager.getWorld(dimension);
            // if world and chunk are loaded and player is not already tracking through vanilla mechanics we need to tell them about the chunk
            if (world != null && ((WorldProxy) world)._sc$chunkExists(chunkX, chunkZ) && !Players.getTrackingChunk(world, chunkX, chunkZ).contains(player)) {
                Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
//                SimplePacketKt.sendTo(chunkPacket(chunk, INIT_NEW), player);
            }
        }
    }

    // remove given chunk view from player, if present
    public static void removeView(EntityPlayer player, int dimension, int chunkX, int chunkZ) {
        long chunkEnc = encodeChunk(dimension, chunkX, chunkZ);
        if (removePlayer(chunkEnc, player)) {
            ((EntityPlayerMPProxy) player)._sc$viewedChunks().remove(chunkEnc);
            WorldServer world = DimensionManager.getWorld(dimension);
            // only send unload packet if world is loaded and player is not still tracking
            if (world != null && !Players.getTrackingChunk(world, chunkX, chunkZ).contains(player)) {
//                SimplePacket.Companion.of(chunkUnloadPacket(world.getChunkFromChunkCoords(chunkX, chunkZ))).sendTo(player);
            }
        }
    }

    // event callbacks //

    // remove players logging out from the lists of chunks they are viewing
    public static void onPlayerLogout(EntityPlayer player) {
        TLongHashSet chunks = ((EntityPlayerMPProxy) player)._sc$viewedChunks();
        byte[] states = chunks._states;
        long[] set = chunks._set;
        for (int idx = chunks.capacity(); idx > 0; ) {
            if (states[--idx] == TPrimitiveHash.FULL) {
                removePlayer(set[idx], player);
            }
        }
    }

    // need to notify all players who are not tracking a chunk already
    // but watching it as a world view about loads and unloads
    public static void onChunkLoad(Chunk chunk) {
        ChunkInstance chunkInstance = chunkData.get(encodeChunk(chunk));
        if (chunkInstance != null && !chunkInstance.players.isEmpty()) {
            Iterator<EntityPlayer> it = chunkInstance.notAlreadyTrackingIterator(chunk.getWorld(), chunk.xPosition, chunk.zPosition);
            if (it.hasNext()) {
//                chunkPacket(chunk, INIT_NEW).sendTo(it);
            }
        }
    }

    public static void onChunkUnload(Chunk chunk) {
        ChunkInstance chunkInstance = chunkData.get(encodeChunk(chunk));
        if (chunkInstance != null && !chunkInstance.players.isEmpty()) {
            Iterator<EntityPlayer> it = chunkInstance.notAlreadyTrackingIterator(chunk.getWorld(), chunk.xPosition, chunk.zPosition);
            if (it.hasNext()) {
//                SimplePacket.Companion.of(chunkUnloadPacket(chunk)).sendTo(it);
            }
        }
    }

    // ASM callbacks //

    public static final String CLASS_NAME = "de/take_weiland/mods/commons/internal/worldview/ServerChunkViewManager";

    public static final String SUPPRESS_UNLOAD_PACKET = "suppressUnloadPacket";

    // called if a vanilla unload packet needs to be suppressed for the given player and chunk
    // this is so that viewed chunks do not get unloaded if the player stops tracking them
    @SuppressWarnings("unused")
    public static boolean suppressUnloadPacket(EntityPlayerMP player, Chunk chunk) {
        return ((EntityPlayerMPProxy) player)._sc$viewedChunks().contains(encodeChunk(chunk));
    }

    public static final String ENHANCE_ACTIVE_CHUNK_SET = "enhanceActiveChunkSet";

    // called to enhance the vanilla "active chunk set"
    // this is needed so that vanilla sees viewed chunks as being tracked by a player
    // otherwise updates do not get processed through IWorldAccess and we cannot detect block changes
    @SuppressWarnings("unused")
    public static void enhanceActiveChunkSet(Set<ChunkPos> activeChunks) {
        byte[] states = chunkData._states;
        long[] set = chunkData._set;
        int i = states.length;

        // manual loop through the hash array to get rid of the iterator
        outer:
        do {
            while (true) {
                if (i-- > 0) {
                    if (states[i] == TPrimitiveHash.FULL) {
                        activeChunks.add(decodeIntoChunkPair(set[i]));
                    }
                } else {
                    break outer;
                }
            }

        } while (true);
    }

    public static final String SEND_NEAR_HOOK = "sendNearFailed";

    // called when ServerConfigurationManager decides that packet does _not_ need to be send to given player
    // because they are not in the radius
    // need to check if the packet needs to be sent anyways because player has a chunk view for the given chunk
    public static void sendNearFailed(EntityPlayerMP player, double x, double y, double z, double radius, int dimension, Packet packet) {
        if (hasChunkInRadius(player, dimension, (int) x, (int) z, radius)) {
            if (dimension != player.dimension) {
                // even though this is setting the field after the packet has been potentially passed into other player's netty threads already
                // this is ok since those other players are ok to see the packet without dimension ID set as they are guaranteed to be in the correct dimension
                ((VanillaPacketProxy) packet)._sc$setTargetDimension(dimension);
            }
//            SimplePacket.Companion.of(packet).sendTo(player);
        }
    }

    private static boolean hasChunkInRadius(EntityPlayerMP player, int dimension, int x, int z, double radius) {
        // convert into chunk coords
        x >>= 4;
        z >>= 4;
        radius /= 16;
        radius *= radius; // preemptively square it for checks below

        TLongHashSet viewedChunks = ((EntityPlayerMPProxy) player)._sc$viewedChunks();
        byte[] states = viewedChunks._states;
        long[] set = viewedChunks._set;
        int idx = viewedChunks.capacity();
        do {
            if (--idx < 0) return false;
            if (states[idx] == TPrimitiveHash.FULL) {
                long enc = set[idx];
                if ((enc & DIMENSION_MASK) == dimension) {
                    enc >>>= CHUNK_X_SHIFT;
                    int dx = x - (int) (enc & CHUNK_COORD_MASK | -(enc & CHUNK_COORD_SIGN_BIT));
                    enc >>>= CHUNK_Z_SHIFT - CHUNK_X_SHIFT;
                    int dz = z - (int) ((enc & CHUNK_COORD_MASK) | -(enc & CHUNK_COORD_SIGN_BIT));

                    if (dx * dx + dz * dz < radius) { // TODO is this right? I hope. this should just ignore the Y dimension entirely
                        return true;
                    }
                }
            }
        } while (true);
    }

    // block change callbacks form ChunkUpdateTracker //

    // yLayers is a bit map of changed 16-high chunk layers
    // send changes to all players not already tracking this chunk
    static void onChunkLayersChanged(Chunk chunk, int yLayers) {
        ChunkInstance chunkInstance = chunkData.get(encodeChunk(chunk));
        if (chunkInstance != null && !chunkInstance.players.isEmpty()) {
            Iterator<EntityPlayer> it = chunkInstance.notAlreadyTrackingIterator(chunk.getWorld(), chunk.xPosition, chunk.zPosition);
            if (it.hasNext()) {
//                chunkPacket(chunk, yLayers).sendTo(it);
            }
        }
    }

    // a single block changed in the given chunk (coordinates are within-chunk coordinates, 0-15)
    static void onSingleBlockChanged(Chunk chunk, int x, int y, int z) {
        ChunkInstance chunkInstance = chunkData.get(encodeChunk(chunk));
        if (chunkInstance != null && !chunkInstance.players.isEmpty()) {
            Iterator<EntityPlayer> it = chunkInstance.notAlreadyTrackingIterator(chunk.getWorld(), chunk.xPosition, chunk.zPosition);
            if (it.hasNext()) {
//                blockChangePacket(chunk.getWorld(), x + (chunk.xPosition << 4), y, z + (chunk.zPosition << 4)).sendTo(it);
            }
        }
    }

    // utility for above //

    private static SimplePacket blockChangePacket(World world, int x, int y, int z) {
//        S23PacketBlockChange packet = new S23PacketBlockChange(x, y, z, world);
//        ((VanillaPacketProxy) packet)._sc$setTargetDimension(world.provider.dimensionId);
//
//        TileEntity te = world.getTileEntity(x, y, z);
//        Packet tePkt;
//        if (te != null && (tePkt = te.getDescriptionPacket()) != null) {
//            ((VanillaPacketProxy) tePkt)._sc$setTargetDimension(world.provider.dimensionId);
//            return createConcatPacket(SimplePacket.of(packet), SimplePacket.of(tePkt));
//        } else {
//            return SimplePacket.of(packet);
//        }
        return null;
    }

    private static Packet chunkUnloadPacket(Chunk chunk) {
//        S21PacketChunkData packet = new S21PacketChunkData(chunk, true, 0);
//        ((VanillaPacketProxy) packet)._sc$setTargetDimension(chunk.worldObj.provider.dimensionId);
//        return packet;
        return null;
    }

    private static SimplePacket chunkPacket(Chunk chunk, int yLayers) {
//        S21PacketChunkData packet;
//        if (yLayers == INIT_NEW) {
//            packet = new S21PacketChunkData(chunk, true, 0xFFFF);
//        } else {
//            packet = new S21PacketChunkData(chunk, false, yLayers);
//        }
//        ((VanillaPacketProxy) packet)._sc$setTargetDimension(chunk.worldObj.provider.dimensionId);
//
//        return createConcatPacket(SimplePacket.of(packet), tileEntityPackets(chunk, yLayers));
        return null;
    }

    private static final SimplePacket[] emptyArr = new SimplePacket[0];

    @SuppressWarnings("unchecked")
    private static SimplePacket[] tileEntityPackets(Chunk chunk, int yLayers) {
//        Collection<TileEntity> tileEntities = ((Map<ChunkPosition, TileEntity>) chunk.chunkTileEntityMap).values();
//        int n = tileEntities.size();
//        if (n == 0) {
//            return emptyArr;
//        } else {
//            int dimId = chunk.worldObj.provider.dimensionId;
//            SimplePacket[] arr = new SimplePacket[n];
//            for (TileEntity te : tileEntities) {
//                if ((yLayers & 1 << (te.yCoord >>> 4)) == 0) continue;
//
//                Packet tePkt = te.getDescriptionPacket();
//                if (tePkt != null) {
//                    ((VanillaPacketProxy) tePkt)._sc$setTargetDimension(dimId);
//                    arr[--n] = SimplePacket.of(tePkt);
//                }
//            }
//            return arr;
//        }
        return emptyArr;
    }

    private static SimplePacket createConcatPacket(SimplePacket a, SimplePacket[] b) {
        return null;
    }

    private static SimplePacket createConcatPacket(SimplePacket a, SimplePacket b) {
        return null;
    }

    // remove player from given chunk
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

    // get or create ChunkInstance for given chunk
    private static ChunkInstance getChunkInstance(long chunkEnc) {
        ChunkInstance chunk = chunkData.get(chunkEnc);
        if (chunk == null) {
            chunk = new ChunkInstance();
            chunkData.put(chunkEnc, chunk);
        }
        return chunk;
    }

    private static final long DIMENSION_MASK = 0xF_FFFFL;

    private static final long CHUNK_COORD_MASK     = 0x3F_FFFFL;
    private static final long CHUNK_X_SHIFT        = 20;
    private static final long CHUNK_Z_SHIFT        = 42;
    private static final long CHUNK_COORD_SIGN_BIT = 0x20_0000L;

    private static long encodeChunk(Chunk chunk) {
        return encodeChunk(chunk.getWorld().provider.getDimension(), chunk.xPosition, chunk.zPosition);
    }

    // encode dimension, chunkX and chunkZ into a single long
    public static long encodeChunk(int dimension, int chunkX, int chunkZ) {
        // chunkX and chunkZ need 22 bits each (world allows block coords -30000000-30000000)
        // this assumes that dimensionID doesn't need more than 20 bits. let's just hope so, otherwise revisit this
        // (could for example use an index into a sorted list of used dim IDs instead)
        if (dimension >= 0x7_FFFF || dimension <= 0xFFF8_0000) {
            throw new IllegalStateException("DimensionID out of range, please notify SevenCommons dev.");
        }
        return (dimension & DIMENSION_MASK) | ((chunkX & CHUNK_COORD_MASK) << CHUNK_X_SHIFT) | ((chunkZ & CHUNK_COORD_MASK) << CHUNK_Z_SHIFT);
    }

    // decode given encoded chunk into a ChunkCoordIntPair, ignoring dimension
    private static ChunkPos decodeIntoChunkPair(long l) {
        l >>>= CHUNK_X_SHIFT;
        int x = (int) ((l & CHUNK_COORD_MASK) | -(l & CHUNK_COORD_SIGN_BIT));

        l >>>= CHUNK_Z_SHIFT - CHUNK_X_SHIFT;
        int z = (int) ((l & CHUNK_COORD_MASK) | -(l & CHUNK_COORD_SIGN_BIT));
        return new ChunkPos(x, z);
    }

    // decode encoded chunk into a DimensionalChunk
    public static DimensionalChunk decodeIntoDimensionalChunk(long l) {
        int dimension = (int) (l & DIMENSION_MASK);

        l >>>= CHUNK_X_SHIFT;
        int x = (int) ((l & CHUNK_COORD_MASK) | -(l & CHUNK_COORD_SIGN_BIT));
        l >>>= CHUNK_Z_SHIFT - CHUNK_X_SHIFT;
        int z = (int) ((l & CHUNK_COORD_MASK) | -(l & CHUNK_COORD_SIGN_BIT));

        return new DimensionalChunk(dimension, x, z);
    }
}
