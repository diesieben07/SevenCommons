package de.take_weiland.mods.commons.util;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SCItemAccessor;
import net.minecraft.util.Icon;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.templates.Type;
import de.take_weiland.mods.commons.templates.Typed;

public final class Items {

	private Items() { }

	public static final void init(Item item, String baseName) {
		String modId = Loader.instance().activeModContainer().getModId();
		
		item.setTextureName(getIconName(modId, baseName));
		item.setUnlocalizedName(getLanguageKey(modId, baseName)); // full unlocalized key is "item.MODID.NAME.name"		
		
		if (item instanceof Typed) {
			SCItemAccessor.setHasSubtypes(item);
			
			if (item instanceof Typed) {
				registerSubtypes(item, baseName); // moved to seperate method to keep people from messing with the Type parameter
			}
		}
		
		GameRegistry.registerItem(item, baseName);
	}
	
	private static <T extends Item & Typed<E>, E extends Type> void registerSubtypes(Item item, String baseName) {
		@SuppressWarnings("unchecked")
		T typed = (T)item;
		for (E type : typed.getTypes()) {
			GameRegistry.registerCustomItemStack(Items.getLanguageKey(baseName, type.getName()), getStack(typed, type));
		}
	}
	
	public static <E extends Type, T extends Item & Typed<E>> String getUnlocalizedName(T item, ItemStack stack) {
		return item.getUnlocalizedName() + "." + Multitypes.getType(item, stack).getName();
	}

	public static final <E extends Type, T extends Item & Typed<E>> ItemStack getStack(T item, E type) {
		return getStack(item, type, 1);
	}
	
	public static final <E extends Type, T extends Item & Typed<E>> ItemStack getStack(T item, E type, int quantity) {
		return new ItemStack(item, quantity, type.getMeta());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final <T extends Item & Typed<?>> void addSubtypes(T item, List stacks) {
		for (Type type : item.getTypes()) {
			stacks.add(new ItemStack(item, 1, type.getMeta()));
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static final <T extends Item & Typed<?>> Icon[] registerIcons(T item, IconRegister register) {
		return registerIcons(item, SCItemAccessor.getIconName(item), register);
	}
	
	@SideOnly(Side.CLIENT)
	static Icon[] registerIcons(Typed<?> typed, String prefix, IconRegister register) {
		Type[] types = typed.getTypes();
		Icon[] icons = new Icon[types.length];
		prefix += "_";
		
		for (int i = 0; i < types.length; i++) {
			icons[i] = register.registerIcon(prefix + types[i].getName());
		}
		
		return icons;
	}

	static String getIconName(String modId, String iconName) {
		return modId + ":" + iconName;
	}
	
	static String getLanguageKey(String modId, String baseName) {
		return modId + "." + baseName;
	}
	
}
