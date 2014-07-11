package de.take_weiland.mods.commons.util;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.world.WorldEvent;

public final class Sides {

	private Sides() {
	}

	/**
	 * gets the logical side (ServerThread/ClientThread) based on the given world
	 *
	 * @param world the world to check
	 * @return the logical side
	 */
	public static Side logical(World world) {
		return world.isRemote ? Side.CLIENT : Side.SERVER;
	}

	/**
	 * convenience method. Identical to {@link Sides#logical(World) Sides.logical(entity.worldObj)}
	 */
	public static Side logical(Entity entity) {
		return logical(entity.worldObj);
	}

	/**
	 * convenience method. Identical to {@link Sides#logical(World) Sides.logical(tileEntity.worldObj)}
	 */
	public static Side logical(TileEntity tileEntity) {
		return logical(tileEntity.worldObj);
	}

	/**
	 * convenience method. Identical to {@link Sides#logical(World) Sides.logical(event.entity.worldObj)}
	 */
	public static Side logical(EntityEvent event) {
		return logical(event.entity.worldObj);
	}

	/**
	 * convenience method. Identical to {@link Sides#logical(World) Sides.logical(event.world)}
	 */
	public static Side logical(WorldEvent event) {
		return logical(event.world);
	}

	/**
	 * Determine the Environment, not the logical side (Integrated Server is still on the Minecraft Client)
	 *
	 * @return Side.SERVER for a Dedicated Server, Side.CLIENT for the Minecraft client
	 */
	public static Side environment() {
		return FMLCommonHandler.instance().getSide();
	}
}
