package de.take_weiland.mods.commons.internal.worldview;

import de.take_weiland.mods.commons.internal.EntityPlayerMPProxy;
import de.take_weiland.mods.commons.internal.WorldProxy;
import de.take_weiland.mods.commons.net.SimplePacket;
import de.take_weiland.mods.commons.util.Players;
import gnu.trove.impl.hash.TPrimitiveHash;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
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

    public static void addView(EntityPlayer player, int dimension, int chunkX, int chunkZ) {
        long chunkEnc = encodeChunk(dimension, chunkX, chunkZ);

        getChunkInstance(chunkEnc).players.add(player);
        ((EntityPlayerMPProxy) player)._sc$viewedChunks().add(chunkEnc);

        WorldServer world = DimensionManager.getWorld(dimension);
        // if world and chunk are loaded and player is not already tracking through vanilla mechanics we need to tell them about the chunk
        if (world != null && ((WorldProxy) world)._sc$chunkExists(chunkX, chunkZ) && !Players.getTrackingChunk(world, chunkX, chunkZ).contains(player)) {
            Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
            chunkPacket(chunk, INIT_NEW).sendTo(player);
        }
    }

    public static void removeView(EntityPlayer player, int dimension, int chunkX, int chunkZ) {
        long chunkEnc = encodeChunk(dimension, chunkX, chunkZ);
        if (removePlayer(chunkEnc, player)) {
            ((EntityPlayerMPProxy) player)._sc$viewedChunks().remove(chunkEnc);
            WorldServer world = DimensionManager.getWorld(dimension);
            if (world != null && !Players.getTrackingChunk(world, chunkX, chunkZ).contains(player)) {
                SimplePacket.wrap(chunkUnloadPacket(world.getChunkFromChunkCoords(chunkX, chunkZ))).sendTo(player);
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
                chunkPacket(chunk, INIT_NEW).sendTo(it);
            }
        }
    }

    public static void onChunkUnload(Chunk chunk) {
        ChunkInstance chunkInstance = chunkData.get(encodeChunk(chunk));
        if (chunkInstance != null && !chunkInstance.players.isEmpty()) {
            Iterator<EntityPlayer> it = chunkInstance.notAlreadyTrackingIterator(chunk.worldObj, chunk.xPosition, chunk.zPosition);
            if (it.hasNext()) {
                SimplePacket.wrap(chunkUnloadPacket(chunk)).sendTo(it);
            }
        }
    }

    // ASM callbacks
    public static final String CLASS_NAME = "de/take_weiland/mods/commons/internal/worldview/ServerChunkViewManager";

    public static final String SUPPRESS_UNLOAD_PACKET = "suppressUnloadPacket";

    @SuppressWarnings("unused")
    public static boolean suppressUnloadPacket(EntityPlayerMP player, Chunk chunk) {
        TLongArrayList viewedChunks = ((EntityPlayerMPProxy) player)._sc$viewedChunks();
        long enc = encodeChunk(chunk);
        boolean contains = viewedChunks.contains(enc);
        if (contains) System.out.println("suppressing unload packet for " + chunk.xPosition + ", " + chunk.zPosition);
        return contains;
    }

    public static final String ENHANCE_ACTIVE_CHUNK_SET = "enhanceActiveChunkSet";

    @SuppressWarnings("unused")
    public static void enhanceActiveChunkSet(Set<ChunkCoordIntPair> activeChunks) {
        byte[] states = chunkData._states;
        long[] set = chunkData._set;
        int i = states.length;

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
    public static void sendNearFailed(EntityPlayerMP player, double x, double y, double z, double radius, int dimension, Packet packet) {
        if (hasChunkInRadius(player, dimension, (int) x, (int) z, radius)) {
            if (dimension != player.dimension) {
                ((VanillaPacketProxy) packet)._sc$setTargetDimension(dimension);
            }
            SimplePacket.wrap(packet).sendTo(player);
        }
    }

    private static boolean hasChunkInRadius(EntityPlayerMP player, int dimension, int x, int z, double radius) {
        // convert into chunk coords
        x >>= 4;
        z >>= 4;
        radius /= 16;
        radius *= radius;

        TLongArrayList viewedChunks = ((EntityPlayerMPProxy) player)._sc$viewedChunks();
        for (int i = 0, len = viewedChunks.size(); i < len; i++) {
            long enc = viewedChunks.getQuick(i);
            if ((enc & DIMENSION_MASK) == dimension) {
                enc >>>= CHUNK_X_SHIFT;
                int dx = x - (int) (enc & CHUNK_COORD_MASK | -(enc & CHUNK_COORD_SIGN_BIT));
                enc >>>= CHUNK_Z_SHIFT - CHUNK_X_SHIFT;
                int dz = z - (int) ((enc & CHUNK_COORD_MASK) | -(enc & CHUNK_COORD_SIGN_BIT));

                if (dx * dx + dz * dz < radius) { // TODO is this right? I hope.
                    return true;
                }
            }
        }

        return false;
    }

    // block change callbacks form ChunkUpdateTracker

    static void onChunkLayersChanged(Chunk chunk, int yLayers) {
        ChunkInstance chunkInstance = chunkData.get(encodeChunk(chunk));
        if (chunkInstance != null && !chunkInstance.players.isEmpty()) {
            Iterator<EntityPlayer> it = chunkInstance.notAlreadyTrackingIterator(chunk.worldObj, chunk.xPosition, chunk.zPosition);
            if (it.hasNext()) {
                chunkPacket(chunk, yLayers).sendTo(it);
            }
        }
    }

    static void onSingleBlockChanged(Chunk chunk, int x, int y, int z) {
        ChunkInstance chunkInstance = chunkData.get(encodeChunk(chunk));
        if (chunkInstance != null && !chunkInstance.players.isEmpty()) {
            Iterator<EntityPlayer> it = chunkInstance.notAlreadyTrackingIterator(chunk.worldObj, chunk.xPosition, chunk.zPosition);
            if (it.hasNext()) {
                blockChangePacket(chunk.worldObj, x + (chunk.xPosition << 4), y, z + (chunk.zPosition << 4)).sendTo(it);
            }
        }
    }

    // misc

    private static SimplePacket blockChangePacket(World world, int x, int y, int z) {
        S23PacketBlockChange packet = new S23PacketBlockChange(x, y, z, world);
        ((VanillaPacketProxy) packet)._sc$setTargetDimension(world.provider.dimensionId);

        TileEntity te = world.getTileEntity(x, y, z);
        Packet tePkt;
        if (te != null && (tePkt = te.getDescriptionPacket()) != null) {
            ((VanillaPacketProxy) tePkt)._sc$setTargetDimension(world.provider.dimensionId);
            return createConcatPacket(SimplePacket.wrap(packet), SimplePacket.wrap(tePkt));
        } else {
            return SimplePacket.wrap(packet);
        }
    }

    private static Packet chunkUnloadPacket(Chunk chunk) {
        S21PacketChunkData packet = new S21PacketChunkData(chunk, true, 0);
        ((VanillaPacketProxy) packet)._sc$setTargetDimension(chunk.worldObj.provider.dimensionId);
        return packet;
    }

    private static SimplePacket chunkPacket(Chunk chunk, int yLayers) {
        S21PacketChunkData packet;
        if (yLayers == INIT_NEW) {
            packet = new S21PacketChunkData(chunk, true, 0xFFFF);
        } else {
            packet = new S21PacketChunkData(chunk, false, yLayers);
        }
        ((VanillaPacketProxy) packet)._sc$setTargetDimension(chunk.worldObj.provider.dimensionId);

        return createConcatPacket(SimplePacket.wrap(packet), tileEntityPackets(chunk, yLayers));
    }

    private static final SimplePacket[] emptyArr = new SimplePacket[0];

    @SuppressWarnings("unchecked")
    private static SimplePacket[] tileEntityPackets(Chunk chunk, int yLayers) {
        Collection<TileEntity> tileEntities = ((Map<ChunkPosition, TileEntity>) chunk.chunkTileEntityMap).values();
        int n = tileEntities.size();
        if (n == 0) {
            return emptyArr;
        } else {
            int dimId = chunk.worldObj.provider.dimensionId;
            SimplePacket[] arr = new SimplePacket[n];
            for (TileEntity te : tileEntities) {
                if ((yLayers & 1 << (te.yCoord >>> 4)) == 0) continue;

                Packet tePkt = te.getDescriptionPacket();
                if (tePkt != null) {
                    ((VanillaPacketProxy) tePkt)._sc$setTargetDimension(dimId);
                    arr[--n] = SimplePacket.wrap(tePkt);
                }
            }
            return arr;
        }
    }

    private static SimplePacket createConcatPacket(SimplePacket a, SimplePacket[] b) {
        class Concat implements SimplePacket {

            @Override
            public void sendToServer() {
                a.sendToServer();
                for (SimplePacket packet : b) {
                    if (packet != null) packet.sendToServer();
                }
            }

            @Override
            public void sendTo(EntityPlayerMP player) {
                a.sendTo(player);
                for (SimplePacket packet : b) {
                    if (packet != null) packet.sendTo(player);
                }
            }
        }
        return new Concat();
    }

    private static SimplePacket createConcatPacket(SimplePacket a, SimplePacket b) {
        return new SimplePacket() {
            @Override
            public void sendToServer() {
                a.sendToServer();
                b.sendToServer();
            }

            @Override
            public void sendTo(EntityPlayerMP player) {
                a.sendTo(player);
                b.sendTo(player);
            }
        };
    }

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

    private static final long DIMENSION_MASK = 0xF_FFFFL;

    private static final long CHUNK_COORD_MASK     = 0x3F_FFFFL;
    private static final long CHUNK_X_SHIFT        = 20;
    private static final long CHUNK_Z_SHIFT        = 42;
    private static final long CHUNK_COORD_SIGN_BIT = 0x20_0000L;


    private static long encodeChunk(Chunk chunk) {
        return encodeChunk(chunk.worldObj.provider.dimensionId, chunk.xPosition, chunk.zPosition);
    }

    public static void main(String[] args) {
        System.out.println(decodeIntoChunkPair(encodeChunk(0, 10, -120)));
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

    private static ChunkCoordIntPair decodeIntoChunkPair(long l) {
        l >>>= CHUNK_X_SHIFT;
        int x = (int) ((l & CHUNK_COORD_MASK) | -(l & CHUNK_COORD_SIGN_BIT));

        l >>>= CHUNK_Z_SHIFT - CHUNK_X_SHIFT;
        int z = (int) ((l & CHUNK_COORD_MASK) | -(l & CHUNK_COORD_SIGN_BIT));
        return new ChunkCoordIntPair(x, z);
    }
}
