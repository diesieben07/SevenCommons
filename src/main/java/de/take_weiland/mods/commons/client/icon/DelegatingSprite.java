package de.take_weiland.mods.commons.client.icon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.IIcon;

/**
 * @author diesieben07
 */
class DelegatingSprite implements IIcon {

    protected final IIcon delegate;

    DelegatingSprite(IIcon delegate) {
        this.delegate = delegate;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getIconWidth() {
        return delegate.getIconWidth();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getIconHeight() {
        return delegate.getIconHeight();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getMinU() {
        return delegate.getMinU();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getMaxU() {
        return delegate.getMaxU();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getMinV() {
        return delegate.getMinV();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getMaxV() {
        return delegate.getMaxV();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getInterpolatedU(double i) {
        float d = getMaxU() - getMinU();
        return getMinU() + d * ((float) i / 16.0F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public float getInterpolatedV(double i) {
        float d = getMaxV() - getMinV();
        return getMinV() + d * ((float) i / 16.0F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getIconName() {
        return delegate.getIconName();
    }
}
