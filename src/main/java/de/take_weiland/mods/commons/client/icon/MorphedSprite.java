package de.take_weiland.mods.commons.client.icon;

import de.take_weiland.mods.commons.internal.RenderAwareSprite;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.IIcon;

/**
 * @author diesieben07
 */
final class MorphedSprite extends DelegatingSprite implements RenderAwareSprite {

    private static final int FLIP_U = 0b100;
    private final byte encode;

    private MorphedSprite(IIcon delegate, byte encode) {
        super(delegate);
        this.encode = encode;
    }

    static Object morph(Object delegate, int rot, boolean flipU, boolean flipV) {
        byte encode = encode(rot, flipU, flipV);
        if (encode == 0) {
            return delegate;
        } else if (delegate instanceof IIcon) {
            return new MorphedSprite((IIcon) delegate, encode);
        } else {
            IconProvider provider = (IconProvider) delegate;
            return (IconProvider) (side, context) -> new MorphedSprite(provider.getIcon(side, context), encode);
        }
    }

    private static byte encode(int rot, boolean flipU, boolean flipV) {
        if (flipV) {
            // flipV is just flipU and 180 rotation
            rot += 2;
            flipU = !flipU;
        }

        rot &= 3;

        int notchRot;
        switch (rot) {
            case 0:
                notchRot = 0;
                break;
            case 1:
                notchRot = 1;
                break;
            case 2:
                notchRot = 3;
                break;
            case 3:
                notchRot = 2;
                break;
            default:
                throw new AssertionError();
        }
        return (byte) (notchRot | (flipU ? FLIP_U : 0));
    }

    @Override
    public IIcon preRender(RenderBlocks rb, int side) {
        applyRotation(rb, encode & 0b11);
        return this;
    }

    @Override
    public void postRender(RenderBlocks rb, int side) {
        applyRotation(rb, 0);
    }

    private void applyRotation(RenderBlocks rb, int n) {
        rb.uvRotateBottom = rb.uvRotateTop = rb.uvRotateNorth = rb.uvRotateSouth = rb.uvRotateWest = rb.uvRotateEast = n;
    }

    @Override
    public float getMinU() {
        if (flipU()) {
            return delegate.getMaxU();
        } else {
            return delegate.getMinU();
        }
    }

    @Override
    public float getMaxU() {
        if (flipU()) {
            return delegate.getMinU();
        } else {
            return delegate.getMaxU();
        }
    }

    private boolean flipU() {
        return (encode & FLIP_U) != 0;
    }

    @Override
    public String toString() {
        return delegate + " rotated " + (encode & 0b11) + 'x' + (flipU() ? ", uFlip" : "");
    }
}
