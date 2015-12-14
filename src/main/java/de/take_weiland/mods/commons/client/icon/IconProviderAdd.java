package de.take_weiland.mods.commons.client.icon;

import de.take_weiland.mods.commons.internal.IconProviderInternal;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

/**
 * @author diesieben07
 */
interface IconProviderAdd extends IconProviderInternal {

    @Override
    default IIcon _sc$getIcon(int side) {
        return ((IconProvider) this).getIcon(side, NullContext.INSTANCE);
    }

    @Override
    default IIcon _sc$getIcon(int side, ItemStack stack) {
        return ((IconProvider) this).getIcon(side, new ItemContextImpl(stack));
    }

    @Override
    default IIcon _sc$getIcon(int side, IBlockAccess world, int x, int y, int z) {
        return ((IconProvider) this).getIcon(side, new WorldContextImpl(world, x, y, z));
    }
}
