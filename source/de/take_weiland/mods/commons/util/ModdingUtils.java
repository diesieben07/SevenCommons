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

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.proxy.NBTListProxy;

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
	
	@SuppressWarnings("unchecked")
	public static final List<EntityPlayer> getAllPlayers() {
		return MinecraftServer.getServer().getConfigurationManager().playerEntityList;
	}
	
	@SuppressWarnings("unchecked")
	public static final Set<String> getOpsRaw() {
		return MinecraftServer.getServer().getConfigurationManager().getOps();
	}
	
	public static final List<EntityPlayer> getOps() {
		ImmutableList.Builder<EntityPlayer> ops = ImmutableList.builder();
		Set<String> opsRaw = getOpsRaw();
		for (EntityPlayer player : getAllPlayers()) {
			if (opsRaw.contains(player.username.toLowerCase().trim())) {
				ops.add(player);
			}
		}
		return ops.build();
	}
	
	public static final boolean isOp(EntityPlayer player) {
		return getOpsRaw().contains(player.username.toLowerCase());
	}
	
	/**
	 * view the given NBTTagList as an iterable list
	 * the type parameter T can be used if you are sure that this list only contains NBT-Tags of the given type
	 * @param nbtList
	 * @return
	 */
	public static final <T extends NBTBase> List<T> iterate(final NBTTagList nbtList) {
		return ((NBTListProxy)nbtList).getWrappedList();
	}
}
