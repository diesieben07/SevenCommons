package de.take_weiland.mods.commons.util;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;
import java.util.Set;

/**
 * <p>Utilities for working with player entities.</p>
 */
public final class Players {

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
	 * <p>Get the serverside player entity for the given name.</p>
	 * @param name the username
	 * @return the player entity or null if no such player was found
	 */
	public static EntityPlayerMP forName(String name) {
		return MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(name);
	}

	private Players() { }

}
