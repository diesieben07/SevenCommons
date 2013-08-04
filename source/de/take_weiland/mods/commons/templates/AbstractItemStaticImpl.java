package de.take_weiland.mods.commons.templates;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SevenCommonsItemInjector;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import de.take_weiland.mods.commons.util.MultiType;
import de.take_weiland.mods.commons.util.MultiTypeManager;

class AbstractItemStaticImpl {

	private AbstractItemStaticImpl() { }
	
	static final <T extends Item & AbstractItemInternal> void construct(T item, String baseName) {
		GameRegistry.registerItem(item, baseName);
		item.setBaseName(Loader.instance().activeModContainer().getModId() + "." + baseName);
	}
	
	static final <T extends Item & AbstractItemInternal> void construct(T item, String baseName, MultiTypeManager<?> manager) {
		construct(item, baseName);
		SevenCommonsItemInjector.setHasSubtypes(item);
	}
	
	static final int obtainId(Configuration config, String name, int defaultId) {
		return config.getItem(name, defaultId).getInt();
	}
	
	static final <T extends Item & AbstractItemInternal> String getUnlocalizedName(T item, ItemStack stack) {
		MultiTypeManager<?> manager = item.getMultiManager();
		if (manager == null) {
			return "item." + item.getBaseName();
		} else {
			MultiType type = manager.getType(stack);
			return "item." + item.getBaseName() + "." + type.getName();
		}
	}
	
}
