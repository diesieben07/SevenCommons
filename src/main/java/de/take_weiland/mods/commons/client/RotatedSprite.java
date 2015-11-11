package de.take_weiland.mods.commons.client;

import de.take_weiland.mods.commons.internal.RenderAwareSprite;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;

/**
 * @author diesieben07
 */
final class RotatedSprite extends DelegatingSprite implements RenderAwareSprite {

    private final int amount;

    RotatedSprite(IIcon delegate, int amount) {
        super(delegate);
        this.amount = amount;
    }

    @Override
    public IIcon preRender(RenderBlocks rb, int side) {
        applyRotation(rb, amount);
        return this;
    }

    @Override
    public void postRender(RenderBlocks rb, int side) {
        System.out.println("hello post");
        applyRotation(rb, 0);
    }

    private void applyRotation(RenderBlocks rb, int n) {
        rb.uvRotateBottom = n;
        rb.uvRotateTop = n;
        rb.uvRotateNorth = n;
        rb.uvRotateSouth = n;
        rb.uvRotateWest = n;
        rb.uvRotateEast = n;
    }

    @Override
    public String toString() {
        return delegate + " rotated " + amount + 'x';
    }
}
