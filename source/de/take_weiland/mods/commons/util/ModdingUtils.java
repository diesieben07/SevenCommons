package de.take_weiland.mods.commons.util;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;

public final class ModdingUtils {

	private ModdingUtils() { }
	
	public static final Side determineSide(Entity entity) {
		return determineSide(entity.worldObj);
	}
	
	public static final Side determineSide(TileEntity tileEntity) {
		return determineSide(tileEntity.worldObj);
	}
	
	public static final Side determineSide(World world) {
		return world.isRemote ? Side.CLIENT : Side.SERVER;
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
