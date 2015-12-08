package de.take_weiland.mods.commons.client.icon;

import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

/**
 * @author diesieben07
 */
final class ItemContextImpl implements IconProvider.Context {

    private final ItemStack stack;

    ItemContextImpl(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public Type type() {
        return Type.ITEM;
    }

    @Nullable
    @Override
    public ItemStack itemStack() {
        return stack;
    }

    @Nullable
    @Override
    public IBlockAccess world() {
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
