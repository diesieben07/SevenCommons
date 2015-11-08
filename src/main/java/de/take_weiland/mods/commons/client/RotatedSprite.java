package de.take_weiland.mods.commons.client;

import de.take_weiland.mods.commons.internal.RenderAwareSprite;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;

/**
 * @author diesieben07
 */
final class RotatedSprite extends DelegatingSprite implements RenderAwareSprite {

    private final int amount;
    private final int face;

    RotatedSprite(IIcon delegate, int amount, int face) {
        super(delegate);
        this.amount = amount;
        this.face = face;
    }

    @Override
    public IIcon preRender(RenderBlocks rb, int side) {
        if (side == face) {
            applyRotation(rb, amount);
        }
        return delegate;
    }

    @Override
    public void postRender(RenderBlocks rb, int side) {
        if (side == face) {
            applyRotation(rb, 0);
        }
    }

    private void applyRotation(RenderBlocks rb, int n) {
        switch (face) {
            case 0:
                rb.uvRotateBottom = n;
                break;
            case 1:
                rb.uvRotateTop = n;
                break;
            case 2:
                rb.uvRotateNorth = n;
                break;
            case 3:
                rb.uvRotateSouth = n;
                break;
            case 4:
                rb.uvRotateWest = n;
                break;
            case 5:
                rb.uvRotateEast = n;
                break;
        }
    }
}
