package de.take_weiland.mods.commons.util;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.inv.Inventories;
import de.take_weiland.mods.commons.meta.HasSubtypes;
import de.take_weiland.mods.commons.meta.MetadataProperty;
import de.take_weiland.mods.commons.meta.Subtype;
import de.take_weiland.mods.commons.templates.SCItemBlock;
import de.take_weiland.mods.commons.templates.TypedItemBlock;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

import static de.take_weiland.mods.commons.util.RegistrationUtil.checkPhase;

public final class Blocks {

	private Blocks() {
	}

	/**
	 * <p>Equivalent to {@link #init(net.minecraft.block.Block, String, Class, String)} using the currently active mod and a default ItemBlock class.</p>
	 *
	 * @param block    the Block instance
	 * @param baseName base name for this block
	 */
	public static void init(Block block, String baseName) {
		init(block, baseName, getItemBlockClass(block, null));
	}

	/**
	 * <p>Equivalent to {@link #init(net.minecraft.block.Block, String, Class, String)} using a default ItemBlock class.</p>
	 *
	 * @param modId    Your ModId
	 * @param block    the Block instance
	 * @param baseName base name for this block
	 */
	public static void init(String modId, Block block, String baseName) {
		init(block, baseName, getItemBlockClass(block, null), modId);
	}

	/**
	 * <p>Equivalent to {@link #init(net.minecraft.block.Block, String, Class, String)} using the currently active mod.</p>
	 *
	 * @param block     the Block instance
	 * @param baseName  base name for this block
	 * @param itemClass the ItemBlock class to use
	 */
	public static void init(Block block, String baseName, Class<? extends ItemBlock> itemClass) {
		// check phase here already so that we don't fail with a NPE on activeModContainer instead
		checkPhase("Block");
		init(block, baseName, itemClass, Loader.instance().activeModContainer().getModId());
	}

	/**
	 * <p>Generic initialization for Blocks:</p>
	 * <ul>
	 * <li>Sets the Block's texture to <tt>modId:baseName</tt>, unless it is already set</li>
	 * <li>Sets the Block's unlocalized name to <tt>modId.baseName</tt>, unless it is already set</li>
	 * <li>Register the block with {@link cpw.mods.fml.common.registry.GameRegistry#registerBlock(net.minecraft.block.Block, Class, String, String)}</li>
	 * <li><i>TODO!</i></li>
	 * </ul>
	 * <p>The ItemBlock class provided should extend {@link de.take_weiland.mods.commons.templates.SCItemBlock} (resp.
	 * {@link de.take_weiland.mods.commons.templates.TypedItemBlock} for Blocks that implement {@link de.take_weiland.mods.commons.meta.HasSubtypes})
	 * to enable all features of SevenCommons for this Block.</p>
	 *
	 * @param block     the Block instance
	 * @param baseName  base name for this block
	 * @param itemClass the ItemBlock class to use
	 * @param modId     Your ModId
	 */
	public static void init(Block block, String baseName, Class<? extends ItemBlock> itemClass, String modId) {
		checkPhase("Block");

		if (SCReflector.instance.getRawIconName(block) == null) {
			block.setTextureName(modId + ":" + baseName);
		}
		if (SCReflector.instance.getRawUnlocalizedName(block) == null) {
			block.setUnlocalizedName(modId + "." + baseName);
		}

		GameRegistry.registerBlock(block, getItemBlockClass(block, itemClass), baseName);

		if (block instanceof HasSubtypes) {
			ItemStacks.registerSubstacks(baseName, getItem(block));
		}
	}

	public static Item getItem(Block block) {
		return Item.itemsList[block.blockID];
	}

	public static Block fromItem(Item item) {
		if (item instanceof ItemBlock) {
			return Block.blocksList[((ItemBlock) item).getBlockID()];
		} else {
			throw new IllegalArgumentException("Not an ItemBlock");
		}
	}

	/**
	 * <p>Generic implementation for {@link net.minecraft.block.Block#breakBlock}. This method drops the contents of any Inventory
	 * associated with the block and should therefor be called before any TileEntity is removed.</p>
	 *
	 * @param block the Block instance
	 * @param world the World
	 * @param x     x position
	 * @param y     y position
	 * @param z     z position
	 * @param meta  the metadata of the block being broken
	 */
	public static void genericBreak(Block block, World world, int x, int y, int z, int meta) {
		if (block.hasTileEntity(meta)) {
			Inventories.spillIfInventory(world.getBlockTileEntity(x, y, z));
		}
	}

	/**
	 * <p>Generic implementation for {@link net.minecraft.block.Block#getSubBlocks(int, net.minecraft.creativetab.CreativeTabs, java.util.List)}.
	 * This method takes the {@link de.take_weiland.mods.commons.meta.HasSubtypes} interface into account.</p>
	 *
	 * @param block the Block instance
	 * @param list  the list to add ItemStacks to
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void getSubBlocksImpl(Block block, List list) {
		if (block instanceof HasSubtypes) {
			// this should technically be <? extends Subtype> but Java has no way of doing this
			MetadataProperty<Subtype> property = ((HasSubtypes<Subtype>) block).subtypeProperty();
			for (Subtype type : property.values()) {
				list.add(property.apply(type, new ItemStack(block)));
			}
		} else {
			list.add(new ItemStack(block));
		}
	}

	private static Class<? extends ItemBlock> getItemBlockClass(Block block, Class<? extends ItemBlock> itemBlock) {
		Class<? extends ItemBlock> defaultClass = getDefaultItemBlock(block);
		if (itemBlock == null) {
			return defaultClass;
		}
		if (!defaultClass.isAssignableFrom(itemBlock)) {
			SevenCommons.LOGGER.warning(String.format("ItemBlock class %s should extend %s to enable all SevenCommons features!", itemBlock.getName(), defaultClass.getSimpleName()));
		}

		return itemBlock;
	}

	private static Class<? extends ItemBlock> getDefaultItemBlock(Block block) {
		return block instanceof HasSubtypes ? TypedItemBlock.class : SCItemBlock.class;
	}
}
