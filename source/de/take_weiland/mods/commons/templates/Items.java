package de.take_weiland.mods.commons.templates;

import java.util.List;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.util.CommonUtils;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

public final class Items {

	private Items() { }

	public static final void init(Item item, String baseName) {
		String modId = Loader.instance().activeModContainer().getModId();
		
		item.func_111206_d(modId + ":" + baseName); // setIconName
		item.setUnlocalizedName(modId + "." + baseName); // full unlocalized key is "item.MODID.NAME.name"		
		
		GameRegistry.registerItem(item, baseName);
	}
	
	public static final <E extends Type, T extends Item & Typed<E>> ItemStack getStack(T item, E type) {
		return getStack(item, type, 1);
	}
	
	public static final <E extends Type, T extends Item & Typed<E>> ItemStack getStack(T item, E type, int quantity) {
		return new ItemStack(item, quantity, type.getMeta());
	}
	
	public static final <E extends Type> E getType(Typed<E> item, ItemStack stack) {
		return CommonUtils.defaultedArrayAccess(item.getTypes(), stack.getItemDamage(), item.getDefault());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final <T extends Item & Typed<?>> void addSubtypes(T item, List stacks) {
		for (Type type : item.getTypes()) {
			stacks.add(new ItemStack(item, 1, type.getMeta()));
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static final Icon[] registerIcons(Typed<?> item, String prefix, IconRegister register) {
		Type[] types = item.getTypes();
		Icon[] icons = new Icon[types.length];
		
		for (int i = 0; i < types.length; i++) {
			icons[i] = register.registerIcon(prefix + types[i].getName());
		}
		
		return icons;
	}
	
}
