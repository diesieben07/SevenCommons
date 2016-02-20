package de.take_weiland.mods.commons.worldview;

import de.take_weiland.mods.commons.internal.EntityPlayerMPProxy;
import de.take_weiland.mods.commons.internal.worldview.ServerChunkViewManager;
import de.take_weiland.mods.commons.util.Players;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Set;

/**
 * <p>Mechanics to keep chunks loaded on the client even if they are out of range for the player or in a different dimension.</p>
 * <p>This does <em>not</em> keep the chunks loaded on the server. The client will only track the chunk if it is currently loaded on the server
 * (either by another player or a chunk loader).</p>
 * <p>These settings do not persist, if the player logs out their chunk views are removed.</p>
 *
 * @author diesieben07
 */
public class ClientChunks {

    /**
     * <p>Keep the given chunk in the given dimension loaded for the given player's client.</p>
     *
     * @param player    the player
     * @param dimension the dimension ID
     * @param chunkX    the x coordinate of the chunk
     * @param chunkZ    the z coordinate of the chunk
     */
    public static void loadChunk(EntityPlayer player, int dimension, int chunkX, int chunkZ) {
        ServerChunkViewManager.addView(Players.checkNotClient(player), dimension, chunkX, chunkZ);
    }

    /**
     * <p>Remove the given chunk in the given dimension from the list of chunks to keep loaded for the player's client.
     * This does not affect vanilla chunk-tracking mechanics.</p>
     *
     * @param player    the player
     * @param dimension the dimension ID
     * @param chunkX    the x coordinate of the chunk
     * @param chunkZ    the z coordinate of the chunk
     */
    public static void unloadChunk(EntityPlayer player, int dimension, int chunkX, int chunkZ) {
        ServerChunkViewManager.removeView(Players.checkNotClient(player), dimension, chunkX, chunkZ);
    }

    /**
     * <p>Check if the given chunk in the given dimension is being loaded for the given player.</p>
     *
     * @param player    the player
     * @param dimension the dimension ID
     * @param chunkX    the x coordinate of the chunk
     * @param chunkZ    the z coordinate of the chunk
     * @return true if the given chunk is being loaded
     */
    public static boolean isChunkLoadedForPlayer(EntityPlayer player, int dimension, int chunkX, int chunkZ) {
        return ((EntityPlayerMPProxy) Players.checkNotClient(player))._sc$viewedChunks().contains(ServerChunkViewManager.encodeChunk(dimension, chunkX, chunkZ));
    }

    /**
     * <p>Get all chunks loaded for the given player.</p>
     *
     * @param player the player
     * @return an unmodifiable set of chunks
     */
    public static Set<DimensionalChunk> getPlayerLoadedChunks(EntityPlayer player) {
        return new PlayerChunkSetImpl(((EntityPlayerMPProxy) Players.checkNotClient(player))._sc$viewedChunks());
    }

}
