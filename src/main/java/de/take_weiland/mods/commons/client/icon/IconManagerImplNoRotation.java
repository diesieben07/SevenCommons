package de.take_weiland.mods.commons.client.icon;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * @author diesieben07
 */
final class IconManagerImplNoRotation implements IconManager {

    private final RotatedDirection front;
    private final IIcon[]          icons;

    public IconManagerImplNoRotation(Map<ForgeDirection, IIcon> icons, RotatedDirection front) {
        this.front = front;
        this.icons = new IIcon[6];
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            this.icons[dir.ordinal()] = icons.get(dir);
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return icons[side];
    }

    @Override
    public int getMeta(int front, int frontRotation) {
        return 0;
    }

    @Override
    public int getMeta(@Nonnull EntityLivingBase placer) {
        return 0;
    }

    @Override
    public RotatedDirection getFront(int meta) {
        return front;
    }

}
