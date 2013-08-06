package de.take_weiland.mods.commons.templates;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;

public abstract class AbstractItem extends Item {

	protected final String baseName;
	protected final String modId;
	
	public AbstractItem(Configuration config, String baseName, int defaultId) {
		this(baseName, config.getItem(baseName, defaultId).getInt());
	}
	
	public AbstractItem(String baseName, int itemId) {
		super(itemId);
		this.baseName = baseName;
		modId = Loader.instance().activeModContainer().getModId();
		
		GameRegistry.registerItem(this, baseName);
		
		func_111206_d(modId + ":" + baseName); // setIconName
	}

	@Override
	public String getLocalizedName(ItemStack stack) { // very bad naming here... returns something like "item.foobar", ".name" gets appended elsewhere
		return "item." + modId + "." + baseName;
	}

}
