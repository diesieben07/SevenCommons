package de.take_weiland.mods.commons.util;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.world.WorldEvent;

public final class Sides {

	private Sides() { }
	
	/**
	 * gets the logical side (ServerThread/ClientThread) based on the given world
	 * @param world
	 * @return the logical side
	 */
	public static final Side logical(World world) {
		return world.isRemote ? Side.CLIENT : Side.SERVER;
	}
	
	/**
	 * convenience method. Identical to {@link Sides#logical(World) Sides.logical(entity.worldObj)} 
	 * @param entity
	 * @return
	 */
	public static final Side logical(Entity entity) {
		return logical(entity.worldObj);
	}
	
	/**
	 * convenience method. Identical to {@link Sides#logical(World) Sides.logical(tileEntity.worldObj)} 
	 * @param tileEntity
	 * @return
	 */
	public static final Side logical(TileEntity tileEntity) {
		return logical(tileEntity.worldObj);
	}
	
	/**
	 * convenience method. Identical to {@link Sides#logical(World) Sides.logical(event.entity.worldObj)} 
	 * @param event
	 * @return
	 */
	public static final Side logical(EntityEvent event) {
		return logical(event.entity.worldObj);
	}
	
	/**
	 * convenience method. Identical to {@link Sides#logical(World) Sides.logical(event.world)} 
	 * @param event
	 * @return
	 */
	public static final Side logical(WorldEvent event) {
		return logical(event.world);
	}
	
	public static final Side environment() {
		return FMLCommonHandler.instance().getSide();
	}
}
