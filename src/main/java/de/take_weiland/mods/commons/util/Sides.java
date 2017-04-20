package de.take_weiland.mods.commons.util;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <p>Helper methods for distinguishing between client and server.</p>
 */
public final class Sides {

    /**
     * <p>Get the logical side (based on {@link World#isRemote}) of the given World.</p>
     * <p>This method is best used with a static import like so:<code><pre>
     *     if (sideOf(world).isClient()) {
     *         // do client-only things
     *     }
     * </pre></code></p>
     *
     * @param world the World
     * @return the logical side
     */
    public static Side sideOf(World world) {
        return world.isRemote ? Side.CLIENT : Side.SERVER;
    }

    /**
     * <p>Get the logical side (based on {@link World#isRemote}) of the given Entity.</p>
     * <p>This method is best used with a static import like so:<code><pre>
     *     if (sideOf(entity).isClient()) {
     *         // do client-only things
     *     }
     * </pre></code></p>
     *
     * @param entity the Entity
     * @return the logical side
     */
    public static Side sideOf(Entity entity) {
        return sideOf(entity.world);
    }

    /**
     * <p>Get the logical side (based on {@link World#isRemote}) of the given TileEntity.</p>
     * <p>This method is best used with a static import like so:<code><pre>
     *     if (sideOf(tileEntity).isClient()) {
     *         // do client-only things
     *     }
     * </pre></code></p>
     *
     * @param tileEntity the TileEntity
     * @return the logical side
     */
    public static Side sideOf(TileEntity tileEntity) {
        return sideOf(tileEntity.getWorld());
    }

    /**
     * <p>Get the environment we are running on. {@code Side.SERVER} for a dedicated server or
     * {@code Side.CLIENT} for the client.</p>
     *
     * @return the environment
     */
    public static Side environment() {
        return FMLCommonHandler.instance().getSide();
    }

    private Sides() {
    }
}
