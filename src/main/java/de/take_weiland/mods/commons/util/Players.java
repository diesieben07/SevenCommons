package de.take_weiland.mods.commons.util;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import de.take_weiland.mods.commons.internal.SevenCommons;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * <p>Utilities for working with player entities.</p>
 */
public final class Players {

	@Nullable
	public static EntityPlayerMP getSPOwner() {
		MinecraftServer server = MinecraftServer.getServer();
		if (server.isDedicatedServer()) {
			return null;
		}
		return forName(server.getServerOwner());
	}

	public static boolean isSPOwner(EntityPlayer player) {
		MinecraftServer server = MinecraftServer.getServer();
		return !server.isDedicatedServer() && player.username.equals(server.getServerOwner());
	}

	/**
	 * <p>Gets an Iterable containing all operators currently online on this server.</p>
	 * <p>To get all operators, use {@link #getOpsRaw()}</p>
	 *
	 * @return all Operators
	 */
	public static Iterable<EntityPlayerMP> getOnlineOps() {
		final Set<String> ops = getOpsRaw();
		return Iterables.filter(getAll(), new Predicate<EntityPlayerMP>() {

			@Override
			public boolean apply(EntityPlayerMP player) {
				return ops.contains(player.username.toLowerCase().trim());
			}

		});
	}

	/**
	 * <p>Get the usernames of all operators on this server.</p>
	 * @return a Set of all usernames
	 */
	@SuppressWarnings("unchecked")
	public static Set<String> getOpsRaw() {
		return MinecraftServer.getServer().getConfigurationManager().getOps();
	}

	/**
	 * <p>Check if the given player is an operator.</p>
	 *
	 * @param player the player to check
	 * @return true if the player is an operator
	 */
	public static boolean isOp(EntityPlayer player) {
		return isOp(player.username);
	}

	/**
	 * <p>Check if the given username is an operator.</p>
	 * @param username the username to check
	 * @return true if the username is an operator
	 */
	public static boolean isOp(String username) {
		return getOpsRaw().contains(username.toLowerCase().trim());
	}

	/**
	 * <p>Get all players currently on the Server.</p>
	 * @return all players
	 */
	@SuppressWarnings("unchecked")
	public static List<EntityPlayerMP> getAll() {
		return MinecraftServer.getServer().getConfigurationManager().playerEntityList;
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
		return MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(name);
	}

	private Players() { }

}
