package de.take_weiland.mods.commons.util;

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

	/**
	 * Performs some generic initialization on the given Item
	 * <ul>
	 * 	<li>sets the IconName to <tt>CurrentModId:baseName</tt></li>
	 * 	<li>sets the UnlocalizedName to <tt>item.CurrentModId.baseName</tt> (Note: the full language key is <tt>item.CurrentModId.baseName.name</tt>)</li>
	 *  <li>If it is a Subtyped item (implementing {@link Typed}):
	 *  	<ul>
	 *  		<li>calls item.setHasSubtypes(true)</li>
	 * 			<li>for each subtype registers a {@link GameRegistry#registerCustomItemStack(String, ItemStack) custom ItemStack} with the name <tt>baseName.SubtypeName</tt></li>
	 *  	</ul></li>
	 *  <li>{@link GameRegistry#registerItem(Item, String) registers the Item} with <tt>baseName</tt></li>
	 * </ul>
	 * @param item
	 * @param baseName
	 */
	public static final void init(Item item, String baseName) {
		String modId = Loader.instance().activeModContainer().getModId();
		
		item.setTextureName(getIconName(modId, baseName));
		item.setUnlocalizedName(Names.combine(modId, baseName)); // full unlocalized key is "item.MODID.NAME.name"		
		
		if (item instanceof Typed) {
			SCItemAccessor.setHasSubtypes(item);
			
			if (item instanceof Typed) {
				Multitypes.registerSubtypes((Typed<?>)item, baseName);
			}
		}
		
		GameRegistry.registerItem(item, baseName);
	}
	
	/**
	 * registers a SubIcon for each Subtype the given Subtyped Item<br>
	 * The names will be <tt>BaseIconName_SubTypeName_Postfix</tt>
	 * @param item
	 * @param register
	 * @return
	 */
	@SideOnly(Side.CLIENT)
	public static final <T extends Item & Typed<?>> Icon[] registerIcons(T item, String postfix, IconRegister register) {
		return registerIcons(item, SCItemAccessor.getIconName(item), "_" + postfix, register);
	}
	
	
	/**
	 * registers all Icons for the given Subtyped Item<br>
	 * The names will be <tt>BaseIconName_SubTypeName</tt>
	 * @param item
	 * @param register
	 * @return
	 */
	@SideOnly(Side.CLIENT)
	public static final <T extends Item & Typed<?>> Icon[] registerIcons(T item, IconRegister register) {
		return registerIcons(item, SCItemAccessor.getIconName(item), "", register);
	}
	
	/**
	 * registers a single Icon for the given Item<br>
	 * the postfix will be appended to the already present Icon name of the Item.<br><br>
	 * Example: If the Item's Icon name is <tt>exampleMod:exampleItem</tt> a call to registerIcon with <tt>foobar</tt> as postfix will result in an icon <tt>exampleMod:exampleItem_foobar</tt>
	 * @param item
	 * @param postfix
	 * @param register
	 * @return
	 */
	@SideOnly(Side.CLIENT)
	public static Icon registerIcon(Item item, String postfix, IconRegister register) {
		return registerIcon(SCItemAccessor.getIconName(item), postfix, register);
	}
	
	@SideOnly(Side.CLIENT)
	static Icon[] registerIcons(Typed<?> typed, final String prefix, final String postfix, final IconRegister register) {
		Type<?>[] types = typed.getTypes();
		Icon[] icons = new Icon[types.length];
		
		for (int i = 0; i < types.length; ++i) {
			icons[i] = register.registerIcon(prefix + "_" + types[i].unlocalizedName() + postfix);
		}
		
		return icons;
	}
	
	@SideOnly(Side.CLIENT)
	static Icon registerIcon(String prefix, String suffix, IconRegister register) {
		return register.registerIcon(prefix + "_" + suffix);
	}

	static String getIconName(String modId, String iconName) {
		return modId + ":" + iconName;
	}
	
}
