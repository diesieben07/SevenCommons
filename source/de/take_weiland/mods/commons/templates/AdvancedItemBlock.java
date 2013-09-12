package de.take_weiland.mods.commons.templates;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class AdvancedItemBlock<T extends Block> extends ItemBlock {

	protected final T block;
	
	@SuppressWarnings("unchecked")
	public AdvancedItemBlock(int itemId, Block block) {
		super(itemId);
		this.block = (T) block;
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata) {
		boolean result = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);
		if (stack.hasDisplayName() && block.hasTileEntity(metadata)) {
			TileEntity te = world.getBlockTileEntity(x, y, z);
			if (te instanceof NameableTileEntity) {
				((NameableTileEntity)te).setCustomName(stack.getDisplayName());
			}
		}
		return result;
	}

	@Override
	public String getUnlocalizedNameInefficiently(ItemStack item) {
		return getUnlocalizedName(item); // some optimization
	}
	
}
