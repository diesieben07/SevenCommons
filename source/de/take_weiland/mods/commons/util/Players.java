package de.take_weiland.mods.commons.util;

import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public final class Players {

	private Players() { }

	/**
	 * gets an Iterable for iterating over all the Operators in this server<br>
	 * If you need a Collection use {@link ImmutableSet#copyOf(Iterable) ImmutableSet.copyOf(Players.getOps())}
	 * @return
	 */
	public static final Iterable<EntityPlayer> getOps() {
		final Set<String> ops = getOpsRaw();
		return Iterables.filter(getAll(), new Predicate<EntityPlayer>() {
			
			@Override
			public boolean apply(EntityPlayer player) {
				return ops.contains(player.username.toLowerCase().trim());
			}
			
		});
	}

	/**
	 * gets a Set containing all operator usernames in lowercase 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final Set<String> getOpsRaw() {
		return MinecraftServer.getServer().getConfigurationManager().getOps();
	}

	/**
	 * returns true if the given player is an operator
	 * @param player the player to check
	 * @return true if the player is an operator
	 */
	public static final boolean isOp(EntityPlayer player) {
		return getOpsRaw().contains(player.username.toLowerCase().trim());
	}

	/**
	 * gets a List of all players currently on the server
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final List<EntityPlayer> getAll() {
		return MinecraftServer.getServer().getConfigurationManager().playerEntityList;
	}

}
