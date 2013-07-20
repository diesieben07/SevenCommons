package de.take_weiland.mods.commons.util;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

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
	
}
