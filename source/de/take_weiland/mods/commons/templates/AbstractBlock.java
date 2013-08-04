package de.take_weiland.mods.commons.templates;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;

public abstract class AbstractBlock<C extends AbstractBlock<C>> extends Block implements BlockCommons, InternalBlockCommons<C> {

	private boolean teFeaturesEnabled = false;
	
	public AbstractBlock(Configuration config, String blockName, int defaultId, Material material) {
		this(config, blockName, defaultId, false, material);
	}
	
	public AbstractBlock(Configuration config, String blockName, int defaultId, boolean isTerrainBlock, Material material) {
		this((isTerrainBlock ? config.getTerrainBlock(Configuration.CATEGORY_BLOCK, blockName, defaultId, null) : config.getBlock(blockName, defaultId)).getInt(), material);
	}
	
	public AbstractBlock(int blockId, Material material) {
		super(blockId, material);
		
		AbstractBlockStaticImpl.construct(this);
	}
	
	@Override
	public void disableTileEntityFeatures() {
		teFeaturesEnabled = false;
	}
	
	@Override
	public boolean teFeaturesEnabled() {
		return teFeaturesEnabled;
	}

	@Override
	public void setTeFeaturesEnabled(boolean value) {
		teFeaturesEnabled = value;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int blockId, int meta) {
		AbstractBlockStaticImpl.breakBlock(this, world, x, y, z, blockId, meta);
		super.breakBlock(world, x, y, z, blockId, meta);
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase living, ItemStack item) {
		AbstractBlockStaticImpl.onBlockPlacedBy(this, world, x, y, z, living, item);
		super.onBlockPlacedBy(world, x, y, z, living, item);
	}

	@Override
	public boolean onBlockEventReceived(World world, int x, int y, int z, int eventId, int eventParam) {
		return AbstractBlockStaticImpl.onBlockEventReceived(this, world, x, y, z, eventId, eventParam);
	}
	
}
