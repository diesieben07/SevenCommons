package de.take_weiland.mods.commons.inv;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

/**
 * <p>An inventory which can be named.</p>
 * <p>If a {@code NameableInventory} is used in a Container it's name will be automatically synchronized with the client.</p>
 * <p>If you implement this on a TileEntity, it will automatically take the name of a renamed ItemStack if placed, override
 * {@link #takeItemStackName(EntityPlayer, ItemStack)} to control that behavior.
 * Keep in mind that this only applies if you either register your block using
 * {@link de.take_weiland.mods.commons.util.Blocks#init(net.minecraft.block.Block, String)} or you use
 * {@link de.take_weiland.mods.commons.templates.SCItemBlock} or a subclass as your ItemBlock class.</p>
 */
public interface NameableInventory extends IInventory {

    /**
     * whether this inventory has been renamed
     *
     * @return true if this inventory has a custom name
     */
    boolean hasCustomName();

    /**
     * set the custom name for this inventory
     *
     * @param name the custom name
     */
    void setCustomName(String name);

    /**
     * get the custom name for this inventory
     *
     * @return the custom name or null if none
     */
    String getCustomName();

    /**
     * <p>Whether this inventory should take the name of a renamed ItemStack when created by a player.
     * This should be called when e.g. a TileEntity implementing this interface is placed or an Entity is spawned, etc.</p>
     *
     * @param player the player
     * @param stack  the ItemStack
     * @return true to take the name
     */
    default boolean takeItemStackName(EntityPlayer player, ItemStack stack) {
        return true;
    }

}
