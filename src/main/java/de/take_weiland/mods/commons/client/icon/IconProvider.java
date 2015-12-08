package de.take_weiland.mods.commons.client.icon;

import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

/**
 * <p>A placeholder for an Icon in an {@link IconManager}. This can be used as a way to specify alternate icons based on
 * data other than rotation. For example a Block could display it's fuel level in it's texture and still use the advantages
 * of {@code IconManager} using this interface.</p>
 * <p>The provided context will give out any information that is passed to the {@link IconManager}, such as an {@code ItemStack}
 * or an {@code IBlockAccess} with coordinates.</p>
 *
 * @author diesieben07
 */
public interface IconProvider {

    /**
     * <p>Provide the icon for the given side in the given context.</p>
     *
     * @param side    the side
     * @param context the context
     * @return the icon
     */
    IIcon getIcon(int side, Context context);

    /**
     * <p>Contextual information for {@link IconProvider#getIcon(int, Context)}.</p>
     */
    interface Context {

        /**
         * <p>The world, this method may only be called when {@link #type()} is {@code WORLD}.</p>
         *
         * @return the world
         */
        IBlockAccess world();

        /**
         * <p>The x coordinate, this method may only be called when {@link #type()} is {@code WORLD}.</p>
         *
         * @return the x coordinate
         */
        int x();

        /**
         * <p>The y coordinate, this method may only be called when {@link #type()} is {@code WORLD}.</p>
         *
         * @return the y coordinate
         */
        int y();

        /**
         * <p>The z coordinate, this method may only be called when {@link #type()} is {@code WORLD}.</p>
         *
         * @return the z coordinate
         */
        int z();

        /**
         * <p>The ItemStack, this method may only be called when {@link #type()} is {@code ITEM}.</p>
         *
         * @return the world
         */
        ItemStack itemStack();

        /**
         * <p>The type of information this context provides.</p>
         *
         * @return the type
         */
        Type type();

        /**
         * <p>Possible types of contexts.</p>
         */
        enum Type {

            /**
             * <p>A world and coordinates are known.</p>
             */
            WORLD,

            /**
             * <p>An ItemStack is known.</p>
             */
            ITEM,

            /**
             * <p>No information can be provided.</p>
             */
            NONE

        }

    }

}
