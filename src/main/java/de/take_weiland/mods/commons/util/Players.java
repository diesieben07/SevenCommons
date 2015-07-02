package de.take_weiland.mods.commons.util;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import de.take_weiland.mods.commons.internal.SCReflector;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.internal.UsernameCache;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * <p>Utilities for working with player entities.</p>
 */
public final class Players {

    /**
     * <p>Helper method for ensuring that the given player is a server-side player.</p>
     *
     * @param player the player
     * @return the player as an EntityPlayerMP
     * @throws IllegalStateException if the player is not a server-side player
     */
    public static EntityPlayerMP checkNotClient(EntityPlayer player) {
        if (player.worldObj.isRemote) {
            throw new IllegalStateException("Expected a serverside player!");
        }
        return (EntityPlayerMP) player;
    }

    /**
     * <p>Gets an Iterable containing all operators currently online on this server.</p>
     *
     * @return all Operators
     */
    public static Iterable<EntityPlayerMP> getOnlineOps() {
        return Iterables.filter(getAll(), Players::isOp);
    }

    /**
     * <p>Check if the given player is an operator.</p>
     *
     * @param player the player to check
     * @return true if the player is an operator
     */
    public static boolean isOp(EntityPlayer player) {
        return isOp(player.getGameProfile());
    }

    /**
     * <p>Check if the player represented by the given GameProfile is an operator.</p>
     *
     * @param profile the GameProfile
     * @return true if the player is an operator
     */
    public static boolean isOp(GameProfile profile) {
        return getSCM().func_152596_g(profile);
    }

    /**
     * <p>Get all players currently on the Server.</p>
     *
     * @return all players
     */
    @SuppressWarnings("unchecked")
    public static List<EntityPlayerMP> getAll() {
        return getSCM().playerEntityList;
    }

    /**
     * <p>Get all players currently in the given World.</p>
     *
     * @param world the world
     * @return all players in the world
     */
    @SuppressWarnings("unchecked")
    public static List<EntityPlayer> allIn(World world) {
        return world.playerEntities;
    }

    /**
     * <p>Get all players currently in the given World.</p>
     *
     * @param world the world
     * @return all players in the world
     */
    @SuppressWarnings("unchecked")
    public static List<EntityPlayerMP> allIn(WorldServer world) {
        return world.playerEntities;
    }

    /**
     * <p>Get all players tracking the chunk with the given coordinates.</p>
     * <p>This method must only be called for server-side worlds and the returned list must not be modified.</p>
     *
     * @param world  the world
     * @param chunkX the x coordinate
     * @param chunkZ the z coordinate
     * @return the players tracking the chunk
     */
    public static List<EntityPlayerMP> getTrackingChunk(World world, int chunkX, int chunkZ) {
        if (world.isRemote) {
            throw new IllegalArgumentException("Cannot get tracking players on the client");
        }
        Object playerInstance = SCReflector.instance.getPlayerInstance(((WorldServer) world).getPlayerManager(), chunkX, chunkZ, false);
        if (playerInstance == null) {
            return Collections.emptyList();
        } else {
            return SCReflector.instance.getWatchers(playerInstance);
        }
    }

    /**
     * <p>Get the client player ({@link net.minecraft.client.Minecraft#thePlayer}) in a safe manner. This method can
     * be referenced in common code without crashing a Dedicated Server, but still must only be called from a client thread.</p>
     *
     * @return the client player
     */
    public static EntityPlayer getClient() {
        return SevenCommons.proxy.getClientPlayer();
    }

    /**
     * <p>Get the server-side player entity for the given name.</p>
     *
     * @param name the username
     * @return the player entity or null if no such player was found
     */
    public static EntityPlayerMP forName(String name) {
        return getSCM().func_152612_a(name);
    }

    /**
     * <p>Get the server-side player entity for the given UUID.</p>
     *
     * @param uuid the UUID
     * @return the player entity or null if no such player was found
     */
    public static EntityPlayerMP forUUID(UUID uuid) {
        for (EntityPlayerMP player : getAll()) {
            if (player.getUniqueID().equals(uuid)) {
                return player;
            }
        }
        return null;
    }

    /**
     * <p>Get the current username for the given UUID. This method will make an asynchronous request to the Mojang API servers when the username is not in the cache.</p>
     * <p>The returned {@code CompletableFuture} will:</p>
     * <ul>
     * <li>be completed already if the UUID is in the cache</li>
     * <li>complete normally when any needed API request finishes successfully</li>
     * <li>complete exceptionally when a needed API request fails</li>
     * </ul>
     *
     * <p>The cache behaves as follows:</p>
     * <ul>
     * <li>When a player joins the game their current username is cached</li>
     * <li>When a request is made to the API the result is cached</li>
     * <li>By default 500 names are cached, this can be changed in the config file</li>
     * <li>The cache may be invalidated for a specific user using {@link #invalidateNameCache(UUID)}. That will guarantee
     * that a new API request is made on the next call to this method, unless the player joins the game in the meantime.</li>
     * </ul>
     *
     * @param uuid the UUID
     * @return a CompletableFuture that completes with the current name of the player
     */
    public static CompletableFuture<String> getName(UUID uuid) {
        return UsernameCache.get(uuid);
    }

    /**
     * <p>Like {@link #getName(UUID)} but does any needed requests synchronously. This method is thereby intended for use in
     * already asynchronous computations and should not be called from the main game thread.</p>
     *
     * @param uuid the UUID
     * @return the current name of the player
     * @throws IOException if an error occurs while requesting the name
     */
    public static String getNameBlocking(UUID uuid) throws IOException {
        return UsernameCache.getBlocking(uuid);
    }

    /**
     * <p>Invalidate any cached name for the given UUID.</p>
     *
     * @param uuid the UUID
     */
    public static void invalidateNameCache(UUID uuid) {
        UsernameCache.invalidate(uuid);
    }

    private static ServerConfigurationManager getSCM() {
        return MinecraftServer.getServer().getConfigurationManager();
    }

    private Players() {
    }

}
