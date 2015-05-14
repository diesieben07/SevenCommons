package de.take_weiland.mods.commons.util;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.reflect.Getter;
import de.take_weiland.mods.commons.reflect.Invoke;
import de.take_weiland.mods.commons.reflect.OverrideTarget;
import de.take_weiland.mods.commons.reflect.SCReflection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

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
		return !getServer().isDedicatedServer() && player.getCommandSenderName().equals(getServer().getServerOwner());
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
	 * @return all players
	 */
	@SuppressWarnings("unchecked")
	public static List<EntityPlayerMP> getAll() {
		return getSCM().playerEntityList;
	}

	/**
	 * <p>Get all players currently in the given World.</p>
	 * @param world the world
	 * @return all players in the world
	 */
	@SuppressWarnings("unchecked")
	public static List<EntityPlayer> allIn(World world) {
		return world.playerEntities;
	}

	/**
	 * <p>Get all players currently in the given World.</p>
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
        Object playerInstance = PlayerManagerAccess.instance.getPlayerInstance(((WorldServer) world).getPlayerManager(), chunkX, chunkZ, false);
        if (playerInstance == null) {
            return Collections.emptyList();
        } else {
            return PlayerManagerAccess.instance.getWatchers(playerInstance);
        }
    }

    private interface PlayerManagerAccess {

        PlayerManagerAccess instance = SCReflection.createAccessor(PlayerManagerAccess.class);

        @Invoke(method = "func_72690_a", srg = true)
        Object getPlayerInstance(PlayerManager playerManager, int chunkX, int chunkZ, boolean create);

        @Getter(field = "field_73263_b", srg = true)
        @OverrideTarget("net.minecraft.server.management.PlayerManager$PlayerInstance")
        List<EntityPlayerMP> getWatchers(Object playerInstance);

    }

	/**
	 * <p>Get the client player ({@link net.minecraft.client.Minecraft#thePlayer} in a safe manner. This method can
	 * be referenced in common code without crashing a Dedicated Server, but still must only be called from a client thread.</p>
	 * @return the client player
	 */
	public static EntityPlayer getClient() {
		return SevenCommons.proxy.getClientPlayer();
	}

	/**
	 * <p>Get the serverside player entity for the given name.</p>
	 * @param name the username
	 * @return the player entity or null if no such player was found
	 */
	public static EntityPlayerMP forName(String name) {
		return getSCM().func_152612_a(name);
	}

	private static ServerConfigurationManager getSCM() {
		return MinecraftServer.getServer().getConfigurationManager();
	}

	private Players() { }

}
