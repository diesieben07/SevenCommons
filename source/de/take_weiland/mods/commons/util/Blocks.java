package de.take_weiland.mods.commons.util;

import net.minecraft.block.Block;
import net.minecraft.block.SCBlockAccessor;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.templates.AdvancedItemBlock;
import de.take_weiland.mods.commons.templates.Type;
import de.take_weiland.mods.commons.templates.Typed;
import de.take_weiland.mods.commons.templates.TypedItemBlock;

public final class Blocks {

	private Blocks() { }
	
	public static final int BLOCK_UPDATE = 1;
	public static final int UPDATE_CLIENTS = 2;
	public static final int PREVENT_RERENDER = 4;
	
	public static final void init(Block block, String baseName) {
		init(block, baseName, block instanceof Typed ? TypedItemBlock.class : AdvancedItemBlock.class);
	}
	
	public static final void init(Block block, String baseName, Class<? extends ItemBlock> itemClass) {
		String modId = Loader.instance().activeModContainer().getModId();
		
		block.setTextureName(Items.getIconName(modId, baseName));
		block.setUnlocalizedName(Items.getLanguageKey(modId, baseName));
		
		GameRegistry.registerBlock(block, itemClass, baseName);
		
		if (block instanceof Typed) {
			Multitypes.registerSubtypes((Typed<?>)block, baseName);
		}
	}
	
	public static <E extends Type, T extends Block & Typed<E>> String getUnlocalizedName(T block, E type) {
		return Items.getLanguageKey(block.getUnlocalizedName(), type.getName());
	}
	
	public static <E extends Type, T extends Block & Typed<E>> String getUnlocalizedName(T block, int meta) {
		return getUnlocalizedName(block, Multitypes.getType(block, meta));
	}
	
	public static <E extends Type, T extends Block & Typed<E>> String getUnlocalizedName(T block, ItemStack stack) {
		return getUnlocalizedName(block, Multitypes.getType(block, stack));
	}
	
	public static final void genericBreak(Block block, World world, int x, int y, int z, int meta) {
		if (block.hasTileEntity(meta)) {
			Inventories.spillIfInventory(world.getBlockTileEntity(x, y, z));
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static final <T extends Block & Typed<?>> Icon[] registerIcons(T block, IconRegister register) {
		return Items.registerIcons(block, SCBlockAccessor.getIconName(block), "", register);
	}
	
	@SideOnly(Side.CLIENT)
	public static final <T extends Block & Typed<?>> Icon[] registerIcons(T block, String postfix, IconRegister register) {
		return Items.registerIcons(block, SCBlockAccessor.getIconName(block), "_" + postfix, register);
	}
	
	@SideOnly(Side.CLIENT)
	public static Icon registerIcon(Block block, IconRegister register, String subName) {
		return Items.registerIcon(SCBlockAccessor.getIconName(block), subName, register);
	}
	
}
