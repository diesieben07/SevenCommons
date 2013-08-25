package de.take_weiland.mods.commons.util;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.SCBlockAccessor;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.templates.Type;
import de.take_weiland.mods.commons.templates.Typed;
import de.take_weiland.mods.commons.templates.TypedItemBlock;

public final class Blocks {

	private Blocks() { }
	
	public static final void init(Block block, String baseName) {
		init(block, baseName, block instanceof Typed ? TypedItemBlock.class : ItemBlock.class);
	}
	
	public static final void init(Block block, String baseName, Class<? extends ItemBlock> itemClass) {
		String modId = Loader.instance().activeModContainer().getModId();
		
		block.func_111022_d(Items.getIconName(modId, baseName)); // set icon name
		block.setUnlocalizedName(Items.getLanguageKey(modId, baseName));
		
		GameRegistry.registerBlock(block, itemClass, baseName);
	}
	
	public static <E extends Type, T extends Block & Typed<E>> String getUnlocalizedName(T block, ItemStack stack) {
		return block.getUnlocalizedName() + "." + Items.getType(block, stack).getName();
	}
	
	public static final void genericBreak(Block block, World world, int x, int y, int z, int meta) {
		if (block.hasTileEntity(meta)) {
			genericBreak(world.getBlockTileEntity(x, y, z));
		}
	}
	
	public static final void genericBreak(TileEntity te) {
		Inventories.spillIfInventory(te);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final <T extends Block & Typed<?>> void addSubtypes(T block, List stacks) {
		for (Type type : block.getTypes()) {
			stacks.add(new ItemStack(block, 1, type.getMeta()));
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static final <T extends Block & Typed<?>> Icon[] registerIcons(T block, IconRegister register) {
		return Items.registerIcons(block, SCBlockAccessor.getIconName(block), register);
	}
	
	public static final <E extends Type, T extends Block & Typed<E>> ItemStack getStack(T item, E type) {
		return getStack(item, type, 1);
	}
	
	public static final <E extends Type, T extends Block & Typed<E>> ItemStack getStack(T item, E type, int quantity) {
		return new ItemStack(item, quantity, type.getMeta());
	}

}
