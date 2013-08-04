package de.take_weiland.mods.commons.templates;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.util.CommonUtils;
import de.take_weiland.mods.commons.util.MultiType;
import de.take_weiland.mods.commons.util.MultiTypeManager;

public abstract class MultiTypedItem<T extends Enum<T> & MultiType> extends AbstractItem {

	protected final MultiTypeManager<T> manager;
	private final Icon[] icons;
	
	public MultiTypedItem(Configuration config, String baseName, int defaultId, Class<T> types, T defaultType) {
		this(baseName, config.getItem(baseName, defaultId).getInt(), types, defaultType);
	}

	public MultiTypedItem(String baseName, int itemId, Class<T> types, T defaultType) {
		super(baseName, itemId);
		setHasSubtypes(true);
		manager = MultiTypeManager.create(this, types, defaultType);
		icons = new Icon[manager.getTypes().length];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int meta) {
		return CommonUtils.safeArrayAccess(icons, meta);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(int itemId, CreativeTabs tab, List itemList) {
		for (T type : manager.getTypes()) {
			itemList.add(manager.getStack(type));
		}
			
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		for (T type : manager.getTypes()) {
			icons[type.ordinal()] = register.registerIcon(modId + ":" + baseName + "_" + type.getName());
		}
	}

	@Override
	public String getLocalizedName(ItemStack stack) {
		return "item." + modId + "." + baseName + "." + manager.getType(stack).getName();
	}
	
	public final T getType(int meta) {
		return manager.getType(meta);
	}
	
	public final T getType(ItemStack stack) {
		return manager.getType(stack);
	}
	
	public final ItemStack getStack(T type) {
		return manager.getStack(type);
	}
	
	public final ItemStack getStack(T type, int quantity) {
		return manager.getStack(type, quantity);
	}
	
	public final ItemStack getUniversalStack() {
		return manager.getUniversalStack();
	}
	
	public final ItemStack getUniversalStack(int quantity) {
		return manager.getUniversalStack(quantity);
	}
	
	public final T[] getTypes() {
		return manager.getTypes();
	}
	
}