package de.take_weiland.mods.commons.client.icon;

import com.google.common.collect.ComparisonChain;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * <p>Pair of a {@link ForgeDirection} and it's rotation.</p>
 *
 * @author diesieben07
 */
public final class RotatedDirection implements Comparable<RotatedDirection> {

    private final ForgeDirection direction;
    private final int            faceRotation;

    public RotatedDirection(ForgeDirection direction, int faceRotation) {
        this.direction = checkFace(direction);
        this.faceRotation = faceRotation & 3;
    }

    static ForgeDirection checkFace(ForgeDirection face) {
        checkArgument(face != ForgeDirection.UNKNOWN, "UNKNOWN not valid");
        return face;
    }

    /**
     * <p>Get the direction.</p>
     *
     * @return the direction
     */
    public ForgeDirection getDirection() {
        return direction;
    }

    /**
     * <p>Get the rotation.</p>
     *
     * @return the rotation
     */
    public int getRotation() {
        return faceRotation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RotatedDirection)) return false;

        RotatedDirection that = (RotatedDirection) o;
        return this.direction == that.direction && this.faceRotation == that.faceRotation;

    }

    static int encode(int front, int frontRotation) {
        return frontRotation | front << 2;
    }

    int encode() {
        return encode(direction.ordinal(), faceRotation);
    }

    @Override
    public int hashCode() {
        return encode();
    }

    @Override
    public int compareTo(@Nonnull RotatedDirection that) {
        return ComparisonChain.start()
                .compare(this.direction, that.direction)
                .compare(this.faceRotation, that.faceRotation)
                .result();
    }

    @Override
    public String toString() {
        return getDirection().toString() + '@' + getRotation();
    }
}
