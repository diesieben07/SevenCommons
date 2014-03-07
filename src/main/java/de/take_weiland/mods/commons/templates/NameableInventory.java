package de.take_weiland.mods.commons.templates;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 *
 */
public interface NameableInventory extends IInventory {

	boolean hasCustomName();

	boolean setCustomName(String name);

	String getCustomName();

	interface TileAutoName extends NameableInventory {

		/**
		 * <p>Return false from here if you don't want this inventory to be automatically named when placed by a renamed
		 * ItemStack.</p>
		 * <p>Keep in mind that this only applies if you either register your block using
		 * {@link de.take_weiland.mods.commons.util.Blocks#init(net.minecraft.block.Block, String)} or you use
		 * {@link de.take_weiland.mods.commons.templates.SCItemBlock} or a subclass as your ItemBlock class.</p>
		 * @return true if this tile should take a renamed ItemStack's name
		 */
		boolean shouldAutoname(EntityPlayer player, ItemStack stack, World world, int x, int y, int z);

	}

}