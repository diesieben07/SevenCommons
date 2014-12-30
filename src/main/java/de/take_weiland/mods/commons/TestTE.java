package de.take_weiland.mods.commons;

import de.take_weiland.mods.commons.sync.Sync;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

/**
* @author diesieben07
*/
public class TestTE extends TileEntity {

	@Sync
	private ItemStack stack;

}
