package de.take_weiland.mods.commons.client.icon;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.ParametersAreNonnullByDefault;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * <p>Manages icons for a block to apply rotations. The type of rotation is specified by a direction for the front face
 * and a rotation for that front face.</p>
 * <p>The metadata generated by {@code getMeta} will depend on the specified rotations. It will always be in the range {@code 0}
 * to {@code (n-1)} where {@code n} is the number of allowed rotations. As such, if the number of allowed rotations is greater
 * than 16, the normal block metadata is not sufficient and an alternate means of storage (e.g. a {@link net.minecraft.tileentity.TileEntity}
 * is needed.</p>
 *
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public interface IconManager {

    /**
     * <p>Get the icon for the given side and metadata.</p>
     * <p>For simple rotations where the rotation metadata is stored in the standard block metadata this method can be
     * used as a drop-in solution for {@link net.minecraft.block.Block#getIcon(int, int)}.</p>
     *
     * @param side  the side
     * @param meta  the metadata
     * @return the icon
     */
    IIcon getIcon(int side, int meta);

    /**
     * <p>Get the icon for the given side and metadata with the given {@code ItemStack} context.</p>
     * <p>The {@code ItemStack} will be passed on to any registered {@link IconProvider IconProviders}.</p>
     *
     * @param stack the ItemStack
     * @param side  the side
     * @param meta  the metadata
     * @return the icon
     */
    IIcon getIcon(ItemStack stack, int side, int meta);

    /**
     * <p>Get the icon for the given side and metadata with the given {@code World} and location context.</p>
     * <p>The {@code World} and location will be passed on to any registered {@link IconProvider IconProviders}.</p>
     *
     * @param side  the side
     * @param meta  the metadata
     * @param world the World
     * @param x     the x coordinate
     * @param y     the y coordinate
     * @param z     the z coordinate
     * @return the icon
     */
    IIcon getIcon(IBlockAccess world, int x, int y, int z, int side, int meta);

    /**
     * <p>Get the metadata for the given front and it's rotation.</p>
     *
     * @param front         the direction the front is facing
     * @param frontRotation the rotation of the front face
     * @return the metadata
     */
    int getMeta(int front, int frontRotation);

    /**
     * <p>Get the metadata for the given front and it's rotation.</p>
     *
     * @param front         the direction the front is facing
     * @param frontRotation the rotation of the front face
     * @return the metadata
     */
    default int getMeta(ForgeDirection front, int frontRotation) {
        checkArgument(front != ForgeDirection.UNKNOWN, "UNKNOWN not valid");
        return getMeta(front.ordinal(), frontRotation);
    }

    /**
     * <p>Determine the orientation based on the rotation angles of the given placer.</p>
     *
     * @param placer the placing entity
     * @return the metadata
     */
    int getMeta(EntityLivingBase placer);

    /**
     * <p>Get the front face that is specified by the given metadata.</p>
     *
     * @param meta the metadata
     * @return the front face and it's rotation
     */
    RotatedDirection getFront(int meta);

}
