package de.take_weiland.mods.commons.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SCItemAccessor;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.templates.HasMetadata;
import de.take_weiland.mods.commons.templates.Metadata.ItemMeta;

public final class Items {

	private Items() { }

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
	@SuppressWarnings("unchecked")
	public static void init(Item item, String baseName) {
		String modId = Loader.instance().activeModContainer().getModId();
		
		item.setTextureName(getIconName(modId, baseName));
		item.setUnlocalizedName(modId + "." + baseName); // full unlocalized key is "item.MODID.NAME.name"		
		
		if (item instanceof HasMetadata) {
			SCItemAccessor.setHasSubtypes(item);
			
			if (item instanceof HasMetadata) {
				ItemStacks.registerAll(((HasMetadata<? extends ItemMeta>)item).getTypes(), baseName, ItemStacks.ITEM_GET_STACK);
			}
		}
		
		GameRegistry.registerItem(item, baseName);
	}
	
	static String getIconName(String modId, String iconName) {
		return modId + ":" + iconName;
	}
	
}
