package de.take_weiland.mods.commons.internal;

import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

/**
 * @author diesieben07
 */
public interface IconProviderInternal {

    String GET_ICON   = "_sc$getIcon";
    String CLASS_NAME = "de/take_weiland/mods/commons/internal/IconProviderInternal";

    default IIcon _sc$getIcon(int side, ItemStack stack) {
        return _sc$getIcon(side);
    }

    default IIcon _sc$getIcon(int side, IBlockAccess world, int x, int y, int z) {
        return _sc$getIcon(side);
    }

    IIcon _sc$getIcon(int side);

}
