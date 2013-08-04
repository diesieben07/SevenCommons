package de.take_weiland.mods.commons.templates;

import de.take_weiland.mods.commons.util.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

// damn you java, i want traits
final class AbstractBlockStaticImpl {

	private AbstractBlockStaticImpl() { }
	
	static final <T extends Block & InternalBlockCommons<? extends T>> void construct(T block) {
		for (int meta = 0; meta < 16; meta++) { // check if any metadata has a TE. if none has we can skip some "heavy" computing
			if (block.hasTileEntity(meta)) {
				block.setTeFeaturesEnabled(true);
				break;
			}
		}
	}
	
	static final <T extends Block & InternalBlockCommons<? extends T>> void breakBlock(T block, World world, int x, int y, int z, int blockId, int meta) {
		if (block.teFeaturesEnabled() && block.hasTileEntity(meta)) {
			TileEntity te = world.getBlockTileEntity(x, y, z);
			InventoryUtils.spillIfInventory(te);
		}
	}
	
	static final <T extends Block & InternalBlockCommons<? extends T>> void onBlockPlacedBy(T block, World world, int x, int y, int z, EntityLivingBase living, ItemStack item) {
		if (block.teFeaturesEnabled() && item.hasDisplayName() && block.hasTileEntity(world.getBlockMetadata(x, y, z))) {
			TileEntity te = world.getBlockTileEntity(x, y, z);
			if (te instanceof TileEntityAbstract<?>) {
				((TileEntityAbstract<?>) te).setCustomName(item.getDisplayName());
			}
		}
	}
	
	static final <T extends Block & InternalBlockCommons<? extends T>> boolean onBlockEventReceived(T block, World world, int x, int y, int z, int eventId, int eventParam) {
		if (block.teFeaturesEnabled() && block.hasTileEntity(world.getBlockMetadata(x, y, z))) {
			return world.getBlockTileEntity(x, y, z).receiveClientEvent(eventId, eventParam);
		} else {
			return false;
		}
	}
	
}
