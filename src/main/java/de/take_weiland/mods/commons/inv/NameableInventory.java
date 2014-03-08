package de.take_weiland.mods.commons.inv;

import net.minecraft.inventory.IInventory;

/**
 * <p>An inventory which can be named.</p>
 * <p>If you implement this on a TileEntity, it will automatically take the name of a renamed ItemStack if placed, use the {@link de.take_weiland.mods.commons.tileentity.TileAutoName}
 * interface to control that behavior.</p>
 * <p>Keep in mind that this only applies if you either register your block using
 * {@link de.take_weiland.mods.commons.util.Blocks#init(net.minecraft.block.Block, String)} or you use
 * {@link de.take_weiland.mods.commons.templates.SCItemBlock} or a subclass as your ItemBlock class.</p>
 */
public interface NameableInventory extends IInventory {

	/**
	 * wheteher this inventory has been renamed
	 * @return true if this inventory has a custom name
	 */
	boolean hasCustomName();

	/**
	 * set the custom name for this inventory
	 * @param name the custom name
	 */
	void setCustomName(String name);

	/**
	 * get the custom name for this inventory
	 * @return the custom name or null if none
	 */
	String getCustomName();

}