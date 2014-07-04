package de.take_weiland.mods.commons.util;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.internal.InstanceCacheHolder;
import de.take_weiland.mods.commons.inv.Inventories;
import de.take_weiland.mods.commons.meta.HasSubtypes;
import de.take_weiland.mods.commons.templates.SCItemBlock;
import de.take_weiland.mods.commons.templates.TypedItemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SCBlockAccessor;
import net.minecraft.item.ItemBlock;
import net.minecraft.world.World;

import static de.take_weiland.mods.commons.util.RegistrationUtil.checkPhase;

public final class Blocks {

	private Blocks() { }

	/**
	 * <p>Equivalent to {@link #init(String, net.minecraft.block.Block, String, Class)} using the currently active mod and a default ItemBlock class.</p>
	 * @param block the Block instance
	 * @param baseName base name for this block
	 */
	public static void init(Block block, String baseName) {
		init(block, baseName, getItemBlockClass(block));
	}

	/**
	 * <p>Equivalent to {@link #init(String, net.minecraft.block.Block, String, Class)} using a default ItemBlock class.</p>
	 * @param modId Your ModId
	 * @param block the Block instance
	 * @param baseName base name for this block
	 */
	public static void init(String modId, Block block, String baseName) {
		init(modId, block, baseName, getItemBlockClass(block));
	}

	/**
	 * <p>Equivalent to {@link #init(String, net.minecraft.block.Block, String, Class)} using the currently active mod.</p>
	 * @param block the Block instance
	 * @param baseName base name for this block
	 * @param itemClass the ItemBlock class to use
	 */
	public static void init(Block block, String baseName, Class<? extends ItemBlock> itemClass) {
		init(Loader.instance().activeModContainer().getModId(), block, baseName, itemClass);
	}

	@SuppressWarnings("rawtypes") // not sure what this warning is about
	private static Class<? extends SCItemBlock> getItemBlockClass(Block block) {
		return block instanceof HasSubtypes ? TypedItemBlock.class : SCItemBlock.class;
	}

	/**
	 * <p>Generic initialization for Blocks.</p>
	 * <p>This does the following setup tasks:</p>
	 * <ol>
	 *    <li>Sets the block's texture to <i>modId</i>:<i>baseName</i>, unless it is already set</li>
	 *    <li>Sets the block's unlocalized name to <i>modId</i>.<i>baseName</i>, unless it is already set</li>
	 *    <li>Register the block with {@link cpw.mods.fml.common.registry.GameRegistry#registerBlock(net.minecraft.block.Block, Class, String, String)}</li>
	 *    <li></li>
	 * </ol>
	 * @param modId Your ModId
	 * @param block the Block instance
	 * @param baseName base name for this block
	 * @param itemClass the ItemBlock class to use
	 */
	public static void init(String modId, Block block, String baseName, Class<? extends ItemBlock> itemClass) {
		checkPhase("Block");

		if (SCBlockAccessor.getIconNameRaw(block) == null) {
			block.setTextureName(modId + ":" + baseName);
		}
		if (SCReflector.instance.getRawUnlocalizedName(block) == null) {
			block.setUnlocalizedName(modId + "." +  baseName);
		}
		
		GameRegistry.registerBlock(block, itemClass, baseName);
		
		if (block instanceof HasSubtypes) {
			ItemStacks.registerSubstacks(baseName, block, InstanceCacheHolder.BLOCK_STACK_FUNCTION);
		}
	}

	/**
	 * <p>Generic implementation for {@link net.minecraft.block.Block#breakBlock}. This method drops the contents of any Inventory
	 * associated with the block and should therefor be called before any TileEntity is removed.</p>
	 * @param block the Block instance
	 * @param world the World
	 * @param x x position
	 * @param y y position
	 * @param z z position
	 * @param meta the metadata of the block being broken
	 */
	public static void genericBreak(Block block, World world, int x, int y, int z, int meta) {
		if (block.hasTileEntity(meta)) {
			Inventories.spillIfInventory(world.getBlockTileEntity(x, y, z));
		}
	}
	
}
