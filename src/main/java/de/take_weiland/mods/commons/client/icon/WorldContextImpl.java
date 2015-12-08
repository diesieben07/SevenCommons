package de.take_weiland.mods.commons.client.icon;

import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

/**
 * @author diesieben07
 */
final class WorldContextImpl implements IconProvider.Context {

    private final IBlockAccess world;

    private final int x, y, z;

    WorldContextImpl(IBlockAccess world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public Type type() {
        return Type.WORLD;
    }

    @Nullable
    @Override
    public IBlockAccess world() {
        return world;
    }

    @Override
    public int x() {
        return x;
    }

    @Override
    public int y() {
        return y;
    }

    @Override
    public int z() {
        return z;
    }

    @Nullable
    @Override
    public ItemStack itemStack() {
        return null;
    }

}
