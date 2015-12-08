package de.take_weiland.mods.commons.client.icon;

import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

/**
 * @author diesieben07
 */
abstract class AbstractIconManager implements IconManager {

    @Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side, int meta) {
        Object icon = getIcon0(side, meta);
        if (icon instanceof IIcon) {
            return (IIcon) icon;
        } else {
            return ((IconProvider) icon).getIcon(side, new WorldContextImpl(world, x, y, z));
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        Object icon = getIcon0(side, meta);
        if (icon instanceof IIcon) {
            return (IIcon) icon;
        } else {
            return ((IconProvider) icon).getIcon(side, NullContext.INSTANCE);
        }
    }

    @Override
    public IIcon getIcon(ItemStack stack, int side, int meta) {
        Object icon = getIcon0(side, meta);
        if (icon instanceof IIcon) {
            return (IIcon) icon;
        } else {
            return ((IconProvider) icon).getIcon(side, new ItemContextImpl(stack));
        }
    }

    abstract Object getIcon0(int side, int meta);

}
