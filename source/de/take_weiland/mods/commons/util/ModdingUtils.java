package de.take_weiland.mods.commons.util;

import java.util.List;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.world.WorldEvent;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import cpw.mods.fml.relauncher.Side;

public final class ModdingUtils {

	private ModdingUtils() { }
	
	/**
	 * gets the logical side (ServerThread/ClientThread) based on the given world
	 * @param world
	 * @return the logical side
	 */
	public static final Side getSide(World world) {
		return world.isRemote ? Side.CLIENT : Side.SERVER;
	}
	
	/**
	 * convenience method. Identical to {@link ModdingUtils#getSide(World) ModdingUtils.getSide(entity.worldObj)} 
	 * @param entity
	 * @return
	 */
	public static final Side getSide(Entity entity) {
		return getSide(entity.worldObj);
	}
	
	/**
	 * convenience method. Identical to {@link ModdingUtils#getSide(World) ModdingUtils.getSide(tileEntity.worldObj)} 
	 * @param tileEntity
	 * @return
	 */
	public static final Side getSide(TileEntity tileEntity) {
		return getSide(tileEntity.worldObj);
	}
	
	/**
	 * convenience method. Identical to {@link ModdingUtils#getSide(World) ModdingUtils.getSide(event.entity.worldObj)} 
	 * @param event
	 * @return
	 */
	public static final Side getSide(EntityEvent event) {
		return getSide(event.entity.worldObj);
	}
	
	/**
	 * convenience method. Identical to {@link ModdingUtils#getSide(World) ModdingUtils.getSide(event.world)} 
	 * @param event
	 * @return
	 */
	public static final Side getSide(WorldEvent event) {
		return getSide(event.world);
	}
	
	private static EntityPlayer activePlayer;
	
	/**
	 * gets the player that is "in charge" of doing stuff at the moment (placing blocks, etc. etc.)
	 * @return
	 */
	public static final EntityPlayer getActivePlayer() {
		return activePlayer;
	}
	
	/**
	 * internal method - DO NOT CALL!
	 * @param player
	 */
	public static final void setActivePlayer(EntityPlayer player) {
		activePlayer = player;
	}
	
	/**
	 * gets a List of all players currently on the server
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final List<EntityPlayer> getAllPlayers() {
		return MinecraftServer.getServer().getConfigurationManager().playerEntityList;
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
	 * gets an Iterable for iterating over all the Operators in this server<br>
	 * If you need a Collection use {@link ImmutableSet#copyOf(Iterable) ImmutableSet.copyOf(ModdingUtils.getOps())}
	 * @return
	 */
	public static final Iterable<EntityPlayer> getOps() {
		final Set<String> ops = getOpsRaw();
		return Iterables.filter(getAllPlayers(), new Predicate<EntityPlayer>() {
			
			@Override
			public boolean apply(EntityPlayer player) {
				return ops.contains(player.username.toLowerCase().trim());
			}
			
		});
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
	 * view the given NBTTagList as a {@link List}
	 * the type parameter T can be used if you are sure that this list only contains NBT-Tags of the given type
	 * @param nbtList the list to view
	 * @return a modifiable list view of the NBTTagList
	 */
	public static final <T extends NBTBase> List<T> asList(NBTTagList nbtList) {
		return ((NBTListProxy)nbtList).getWrappedList();
	}
	
}
