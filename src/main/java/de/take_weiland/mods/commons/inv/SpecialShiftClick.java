package de.take_weiland.mods.commons.inv;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * <p>Can be implemented on your Container to provide non-standard behavior for
 * {@link de.take_weiland.mods.commons.inv.Containers#handleShiftClick(net.minecraft.inventory.Container, net.minecraft.entity.player.EntityPlayer, int)}.</p>
 *
 * @author diesieben07
 */
public interface SpecialShiftClick {

    /**
     * <p>Get the target for the given ItemStack.</p>
     *
     * @param stack  the ItemStack being transferred
     * @param player the player
     * @return a ShiftClickTarget
     */
    ShiftClickTarget getShiftClickTarget(ItemStack stack, EntityPlayer player);

}
