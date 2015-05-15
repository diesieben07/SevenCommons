package de.take_weiland.mods.commons.util;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

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
        return logical(tileEntity.getWorldObj());
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
