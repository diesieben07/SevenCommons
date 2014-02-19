package de.take_weiland.mods.commons.util;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;
import java.util.Set;

public final class Players {

	private Players() { }

	/**
	 * gets an Iterable for iterating over all the Operators in this server<br />
	 * If you only need the usernames, you should use {@link #getOpsRaw()}.
	 * If you need a Collection use {@link ImmutableSet#copyOf(Iterable) ImmutableSet.copyOf(Players.getOps())}
	 * @return
	 */
	public static Iterable<EntityPlayerMP> getOps() {
		final Set<String> ops = getOpsRaw();
		return Iterables.filter(getAll(), new Predicate<EntityPlayerMP>() {
			
			@Override
			public boolean apply(EntityPlayerMP player) {
				return ops.contains(player.username.toLowerCase().trim());
			}
			
		});
	}

	/**
	 * gets a Set containing all operator usernames in lowercase 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Set<String> getOpsRaw() {
		return MinecraftServer.getServer().getConfigurationManager().getOps();
	}

	/**
	 * returns true if the given player is an operator
	 * @param player the player to check
	 * @return true if the player is an operator
	 */
	public static boolean isOp(EntityPlayer player) {
		return getOpsRaw().contains(player.username.toLowerCase().trim());
	}

	/**
	 * gets a List of all players currently on the server
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<EntityPlayerMP> getAll() {
		return MinecraftServer.getServer().getConfigurationManager().playerEntityList;
	}
	
	@SuppressWarnings("unchecked")
	public static List<EntityPlayer> getAll(World world) {
		return world.playerEntities;
	}
	
	@SuppressWarnings("unchecked")
	public static List<EntityPlayerMP> getAll(WorldServer world) {
		return world.playerEntities;
	}
	
	public static EntityPlayerMP forName(String name) {
		return MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(name);
	}
	
}
