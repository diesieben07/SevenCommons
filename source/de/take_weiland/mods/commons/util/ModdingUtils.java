package de.take_weiland.mods.commons.util;

import java.util.List;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.google.common.collect.ImmutableList;

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
	 * convenience method. Identical to {@link ModdingUtils#getSide(World) ModdingUtils.getSide(entity.worldObj)} with 
	 * @param tileEntity
	 * @return
	 */
	public static final Side getSide(Entity entity) {
		return getSide(entity.worldObj);
	}
	
	/**
	 * convenience method. Identical to {@link ModdingUtils#getSide(World) ModdingUtils.getSide(tileEntity.worldObj)} with 
	 * @param tileEntity
	 * @return
	 */
	public static final Side getSide(TileEntity tileEntity) {
		return getSide(tileEntity.worldObj);
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
}
