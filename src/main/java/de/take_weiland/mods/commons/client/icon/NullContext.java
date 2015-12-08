package de.take_weiland.mods.commons.client.icon;

import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

/**
 * @author diesieben07
 */
enum NullContext implements IconProvider.Context {

    INSTANCE;

    @Override
    public Type type() {
        return Type.NONE;
    }

    @Nullable
    @Override
    public IBlockAccess world() {
        return null;
    }

    @Nullable
    @Override
    public ItemStack itemStack() {
        return null;
    }

    @Override
    public int x() {
        return Integer.MIN_VALUE;
    }

    @Override
    public int y() {
        return Integer.MIN_VALUE;
    }

    @Override
    public int z() {
        return Integer.MIN_VALUE;
    }

}
