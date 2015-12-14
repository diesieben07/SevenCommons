package de.take_weiland.mods.commons.client.icon;

import de.take_weiland.mods.commons.internal.ASMHooks;
import de.take_weiland.mods.commons.internal.IconProviderInternal;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

/**
 * @author diesieben07
 */
abstract class AbstractIconManager implements IconManager {

    @Override
    public IIcon getIcon(int side, int meta) {
        // this is as ugly as your mom, get over it.
        return getIcon0(side, ASMHooks.drawBlockInv ? inventoryMeta() : meta)._sc$getIcon(side);
    }

    @Override
    public IIcon getIcon(ItemStack stack, int side) {
        return getIcon0(side, inventoryMeta())._sc$getIcon(side, stack);
    }

    @Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side, int meta) {
        return getIcon0(side, meta)._sc$getIcon(side, world, x, y, z);
    }

    @Override
    public IIcon getInventoryIcon(int side) {
        return getIcon0(side, inventoryMeta())._sc$getIcon(side);
    }

    abstract int inventoryMeta();

    abstract IconProviderInternal getIcon0(int side, int meta);

}
