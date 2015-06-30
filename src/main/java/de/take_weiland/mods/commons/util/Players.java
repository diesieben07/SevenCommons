package de.take_weiland.mods.commons.util;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.mojang.authlib.GameProfile;
import de.take_weiland.mods.commons.internal.SCReflector;
import de.take_weiland.mods.commons.internal.SevenCommons;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

import static net.minecraft.server.MinecraftServer.getServer;

/**
 * <p>Utilities for working with player entities.</p>
 */
public final class Players {

    @Nullable
    public static EntityPlayerMP getSPOwner() {
        if (getServer().isDedicatedServer()) {
            return null;
        }
        return forName(getServer().getServerOwner());
    }

    public static boolean isSPOwner(EntityPlayer player) {
        return player.worldObj.isRemote && getServer() != null;
    }

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
     * <p>Get the client player ({@link net.minecraft.client.Minecraft#thePlayer} in a safe manner. This method can
     * be referenced in common code without crashing a Dedicated Server, but still must only be called from a client thread.</p>
     *
     * @return the client player
     */
    public static EntityPlayer getClient() {
        return SevenCommons.proxy.getClientPlayer();
    }

    /**
     * <p>Get the serverside player entity for the given name.</p>
     *
     * @param name the username
     * @return the player entity or null if no such player was found
     */
    public static EntityPlayerMP forName(String name) {
        return getSCM().func_152612_a(name);
    }

    /**
     * <p>Get the serverside player entity for the given UUID.</p>
     *
     * @param uuid the UUID
     * @return the player entity or null if no such player was found
     */
    public static EntityPlayerMP forUUID(UUID uuid) {
        for (EntityPlayerMP player : getAll()) {
            if (Objects.equals(player.getUniqueID(), uuid)) {
                return player;
            }
        }
        return null;
    }

    public static String getLastUsernameKnown(UUID uuid) {
        EntityPlayerMP online = forUUID(uuid);
        if (online != null) {
            return online.getCommandSenderName();
        }

        GameProfile gameProfile = getServer().func_152358_ax().func_152652_a(uuid);
        return gameProfile == null ? null : gameProfile.getName();
    }

    private static final String USERNAME_API_URL = "https://api.mojang.com/user/profiles/%s/names";
    private static final CharMatcher DASH_MATCHER = CharMatcher.is('-');
    private static final Gson GSON = new Gson();

    public static CompletableFuture<String> getLastUsername(UUID uuid) {
        EntityPlayerMP online = forUUID(uuid);
        if (online != null) {
            return CompletableFuture.completedFuture(online.getCommandSenderName());
        }

        CompletableFuture<String> future = new CompletableFuture<>();

        ForkJoinPool.commonPool().execute(() -> {
            String uuidString = DASH_MATCHER.removeFrom(uuid.toString());
            try (BufferedReader reader = Resources.asCharSource(new URL(String.format(USERNAME_API_URL, uuidString)), StandardCharsets.UTF_8).openBufferedStream()) {
                JsonReader json = new JsonReader(reader);
                json.beginArray();

                String name = null;
                long when = 0;

                while (json.hasNext()) {
                    String nameObj = null;
                    long timeObj = 0;
                    json.beginObject();
                    while (json.hasNext()) {
                        String key = json.nextName();
                        switch (key) {
                            case "name":
                                nameObj = json.nextString();
                                break;
                            case "changedToAt":
                                timeObj = json.nextLong();
                                break;
                            default:
                                json.skipValue();
                                break;
                        }
                    }
                    json.endObject();

                    if (nameObj != null && timeObj >= when) {
                        name = nameObj;
                    }
                }

                json.endArray();

                future.complete(name);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    private static ServerConfigurationManager getSCM() {
        return MinecraftServer.getServer().getConfigurationManager();
    }

    private Players() {
    }

}
