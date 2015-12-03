package de.take_weiland.mods.commons.client.icon;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * <p>A builder for {@link IconManager}.</p>
 * <p>All methods for specifying textures expect the textures to be specified using the default orientation: front facing north, up facing up.</p>
 * <p>Most methods return {@code this} to allow a fluid-style initialization.</p>
 *
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public interface IconManagerBuilder {
    /**
     * <p>Add the given list of directions as allowed front facings.</p>
     *
     * @param directions the list of directions
     * @return this
     */
    default IconManagerBuilder addValidFront(ForgeDirection... directions) {
        for (ForgeDirection dir : directions) {
            addValidFront(new RotatedDirection(dir, 0));
        }
        return this;
    }

    /**
     * <p>Add the given direction in the given rotation as an allowed front facing.</p>
     *
     * @param direction the direction
     * @param rotation  the rotation
     * @return this
     */
    default IconManagerBuilder addValidFront(ForgeDirection direction, int rotation) {
        return addValidFront(new RotatedDirection(direction, rotation));
    }

    /**
     * <p>Add the given list of directions with rotations 0-3 as allowed front facings.</p>
     *
     * @param directions the list of directions
     * @return this
     */
    default IconManagerBuilder addValidRotatableFront(ForgeDirection... directions) {
        for (ForgeDirection direction : directions) {
            for (int i = 0; i < 4; i++) {
                addValidFront(new RotatedDirection(direction, i));
            }
        }
        return this;
    }

    /**
     * <p>Add the four cardinal directions (north, east, south, west) as allowed front facings.</p>
     *
     * @return this
     */
    default IconManagerBuilder addCardinalDirections() {
        return addCardinalDirections(false);
    }

    /**
     * <p>Add the given list of directions with the specified rotations as allowed front facings.</p>
     *
     * @param directions the list of directions
     * @return this
     */
    IconManagerBuilder addValidFront(RotatedDirection... directions);

    /**
     * <p>Add the four cardinal directions (north, east, south, west) as allowed front facings. If {@code allowFrontRotation} is {@code true},
     * the front faces may rotate.</p>
     *
     * @param allowFrontRotation whether the front faces may rotate
     * @return this
     */
    default IconManagerBuilder addCardinalDirections(boolean allowFrontRotation) {
        ForgeDirection[] arr = new ForgeDirection[]{ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST};
        if (allowFrontRotation) {
            return addValidRotatableFront(arr);
        } else {
            return addValidFront(arr);
        }
    }

    /**
     * <p>Add all possible 24 cube orientations.</p>
     *
     * @return this
     */
    default IconManagerBuilder addAllRotations() {
        return addValidRotatableFront(ForgeDirection.VALID_DIRECTIONS);
    }

    /**
     * <p>Set a default resource domain to be used for future texture specifications. After {@code defaultResourceDomain("example")}
     * the icon {@code "stone"} will no longer resolve to {@code "minecraft:stone"} but to {@code "example:stone"}.</p>
     *
     * @param domain the texture domain
     * @return this
     */
    IconManagerBuilder defaultResourceDomain(String domain);

    /**
     * <p>The {@link IIconRegister} associated with this builder.</p>
     *
     * @return the icon register
     */
    IIconRegister register();

    /**
     * <p>Apply the given icon to given list of faces.</p>
     *
     * @param icon  the icon
     * @param faces the face
     * @return this
     */
    IconManagerBuilder texture(IIcon icon, ForgeDirection... faces);

    /**
     * <p>Apply the given icon to given list of faces.</p>
     *
     * @param icon  the icon
     * @param faces the face
     * @return this
     */
    default IconManagerBuilder texture(String icon, ForgeDirection... faces) {
        return texture(register().registerIcon(icon), faces);
    }

    /**
     * <p>Apply the given icon to all faces.</p>
     *
     * @param icon the icon
     * @return this
     */
    default IconManagerBuilder texture(IIcon icon) {
        return texture(icon, ForgeDirection.VALID_DIRECTIONS);
    }

    /**
     * <p>Apply the given icon to all faces.</p>
     *
     * @param icon the icon
     * @return this
     */
    default IconManagerBuilder texture(String icon) {
        return texture(register().registerIcon(icon));
    }

    /**
     * <p>Apply the given icon to all faces except top and bottom.</p>
     *
     * @param icon the icon
     * @return this
     */
    default IconManagerBuilder textureSides(IIcon icon) {
        return texture(icon, ForgeDirection.NORTH, ForgeDirection.EAST, ForgeDirection.SOUTH, ForgeDirection.WEST);
    }

    /**
     * <p>Apply the given icon to all faces except top and bottom.</p>
     *
     * @param icon the icon
     * @return this
     */
    default IconManagerBuilder textureSides(String icon) {
        return textureSides(register().registerIcon(icon));
    }

    /**
     * <p>Apply the given icon to the top and bottom face.</p>
     *
     * @param icon the icon
     * @return this
     */
    default IconManagerBuilder textureTopBottom(IIcon icon) {
        return texture(icon, ForgeDirection.DOWN, ForgeDirection.UP);
    }

    /**
     * <p>Apply the given icon to the top and bottom face.</p>
     *
     * @param icon the icon
     * @return this
     */
    default IconManagerBuilder textureTopBottom(String icon) {
        return textureTopBottom(register().registerIcon(icon));
    }

    /**
     * <p>Create a {@link IconManager} based on the specifications.</p>
     * <p>This method ensures that the resulting {@code IconManager} can be used with standard block metadata (no more than
     * 16 possible rotations). To bypass this security check, use {@link #build(boolean) build(false)} instead.</p>
     *
     * @return an {@code IconManager}
     */
    default IconManager build() {
        return build(true);
    }

    /**
     * <p>Create a {@link IconManager} based on the specifications.</p>
     *
     * @param useStandardMeta whether to ensure there are no more than 16 rotations specified to ensure compatibility with
     *                        standard block metadata
     * @return an {@code IconManager}
     */
    IconManager build(boolean useStandardMeta);

}
