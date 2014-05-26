package de.take_weiland.mods.commons.util;

import com.google.common.base.Function;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.meta.HasSubtypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SCItemAccessor;

public final class Items {

	private Items() { }

	private static final Function<Item, ItemStack> STACK_FUNCTION = new Function<Item, ItemStack>() {
		@Override
		public ItemStack apply(Item item) {
			return new ItemStack(item);
		}
	};

	public static void init(Item item, String baseName) {
		init(item, baseName,Loader.instance().activeModContainer().getModId());
	}

	/**
	 * Performs some generic initialization on the given Item
	 * <ul>
	 * 	<li>sets the IconName to <tt>CurrentModId:baseName</tt></li>
	 * 	<li>sets the UnlocalizedName to <tt>item.CurrentModId.baseName</tt> (Note: the full language key is <tt>item.CurrentModId.baseName.name</tt>)</li>
	 *  <li>If it is a Subtyped item (implementing {@link HasMetadata}):
	 *  	<ul>
	 *  		<li>calls item.setHasSubtypes(true)</li>
	 * 			<li>for each subtype registers a {@link GameRegistry#registerCustomItemStack(String, ItemStack) custom ItemStack} with the name <tt>baseName.SubtypeName</tt></li>
	 *  	</ul></li>
	 *  <li>{@link GameRegistry#registerItem(Item, String) registers the Item} with <tt>baseName</tt></li>
	 * </ul>
	 * @param item
	 * @param baseName
	 */
	public static void init(Item item, String baseName, String modId) {
		item.setTextureName(modId + ":" + baseName);
		item.setUnlocalizedName(modId + "." + baseName); // full unlocalized key is "item.MODID.NAME.name"		
		
		if (item instanceof HasSubtypes) {
			SCItemAccessor.setHasSubtypes(item);
			
			ItemStacks.registerSubstacks(baseName, item, STACK_FUNCTION);
		}
		
		GameRegistry.registerItem(item, baseName);
	}

}
