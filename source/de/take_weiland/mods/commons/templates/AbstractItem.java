package de.take_weiland.mods.commons.templates;

import de.take_weiland.mods.commons.util.MultiTypeManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;

public abstract class AbstractItem extends Item implements AbstractItemInternal {

	private MultiTypeManager<?> manager;
	private String baseName;
	
	public AbstractItem(MultiTypeManager<?> manager, Configuration config, String baseName, int defaultId) {
		this(manager, baseName, AbstractItemStaticImpl.obtainId(config, baseName, defaultId));
	}
	
	public AbstractItem(MultiTypeManager<?> manager, String baseName, int itemId) {
		super(itemId);
		AbstractItemStaticImpl.construct(this, baseName, manager);
	}
	
	public AbstractItem(Configuration config, String baseName, int defaultId) {
		this(baseName, AbstractItemStaticImpl.obtainId(config, baseName, defaultId));
	}
	
	public AbstractItem(String baseName, int itemId) {
		super(itemId);
		AbstractItemStaticImpl.construct(this, baseName);
	}

	@Override
	public final MultiTypeManager<?> getMultiManager() {
		return manager;
	}

	@Override
	public final void setMultiManager(MultiTypeManager<?> manager) {
		this.manager = manager;
	}

	@Override
	public final String getBaseName() {
		return baseName;
	}
	
	@Override
	public final void setBaseName(String baseName) {
		this.baseName = baseName;
	}

	@Override
	public String getLocalizedName(ItemStack stack) { // very bad naming here... returns something like "item.foobar", ".name" gets appended elsewhere
		return AbstractItemStaticImpl.getUnlocalizedName(this, stack);
	}

}
