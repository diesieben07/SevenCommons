package de.take_weiland.mods.commons.templates;

import de.take_weiland.mods.commons.inv.NameableInventory;
import de.take_weiland.mods.commons.tileentity.TileAutoName;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class SCItemBlock<T extends Block> extends ItemBlock {

	protected final T block;

	@SuppressWarnings("unchecked")
	public SCItemBlock(int itemId, Block block) {
		super(itemId);
		this.block = (T) block;
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata) {
		boolean result;
		if ((result = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata))) {
			doSCPlaceFeatures(stack, player, world, x, y, z, side);
		}
		return result;
	}

	protected final void doSCPlaceFeatures(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int meta) {
		if (stack.hasDisplayName() && block.hasTileEntity(meta)) {
			TileEntity te = world.getBlockTileEntity(x, y, z);
			if (te instanceof NameableInventory &&
					(!(te instanceof TileAutoName) ||
							((TileAutoName) te).shouldAutoname(player, stack, world, x, y, z))) {
				((NameableInventory) te).setCustomName(stack.getDisplayName());
			}
		}
	}

	@Override
	public String getUnlocalizedNameInefficiently(ItemStack item) {
		return getUnlocalizedName(item); // some optimization
	}

}
