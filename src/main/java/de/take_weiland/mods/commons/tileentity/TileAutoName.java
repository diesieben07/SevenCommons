package de.take_weiland.mods.commons.tileentity;

import de.take_weiland.mods.commons.inv.NameableInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * <p>Used to control automatic naming of TileEntities implementing {@link de.take_weiland.mods.commons.inv.NameableInventory}.</p>
 */
public interface TileAutoName extends NameableInventory {

	/**
	 * <p>Return false from here if you don't want this inventory to be automatically named when placed by a renamed
	 * ItemStack.</p>
	 * @param player the player
	 * @param stack the ItemStack
	 * @param world the World
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @return true if this TileEntity should take a renamed ItemStack's name
	 */
	boolean shouldAutoname(EntityPlayer player, ItemStack stack, World world, int x, int y, int z);

}
