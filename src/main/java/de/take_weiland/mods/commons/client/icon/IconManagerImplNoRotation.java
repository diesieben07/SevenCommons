package de.take_weiland.mods.commons.client.icon;

import de.take_weiland.mods.commons.internal.IconProviderInternal;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Map;

/**
 * @author diesieben07
 */
final class IconManagerImplNoRotation extends AbstractIconManager {

    private final RotatedDirection       front;
    private final IconProviderInternal[] icons;

    public IconManagerImplNoRotation(Map<ForgeDirection, IconProviderInternal> icons, RotatedDirection front) {
        this.front = front;
        this.icons = new IconProviderInternal[6];
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            this.icons[dir.ordinal()] = icons.get(dir);
        }
    }

    @Override
    IconProviderInternal getIcon0(int side, int meta) {
        return icons[side];
    }

    @Override
    int inventoryMeta() {
        return 0;
    }

    @Override
    public int getMeta(int front, int frontRotation) {
        return 0;
    }

    @Override
    public int getMeta(EntityLivingBase placer) {
        return 0;
    }

    @Override
    public RotatedDirection getFront(int meta) {
        return front;
    }

}
