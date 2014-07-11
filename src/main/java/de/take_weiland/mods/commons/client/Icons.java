package de.take_weiland.mods.commons.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.meta.HasSubtypes;
import de.take_weiland.mods.commons.meta.MetadataProperty;
import de.take_weiland.mods.commons.meta.Subtype;
import de.take_weiland.mods.commons.util.SCReflector;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.Item;
import net.minecraft.util.Icon;

import java.util.Map;

@SideOnly(Side.CLIENT)
public final class Icons {

	public static <TYPE extends Subtype, ITEM extends Item & HasSubtypes<TYPE>> Map<TYPE, Icon> registerMulti(ITEM item, IconRegister register) {
		return registerMulti0(SCReflector.instance.getIconName(item) + ".", item, register);
	}

	public static <TYPE extends Subtype, BLOCK extends Block & HasSubtypes<TYPE>> Map<TYPE, Icon> registerMulti(BLOCK block, IconRegister register) {
		return registerMulti0(SCReflector.instance.getIconName(block) + ".", block, register);
	}

	private static <TYPE extends Subtype> Map<TYPE, Icon> registerMulti0(String base, HasSubtypes<TYPE> element, IconRegister register) {
		MetadataProperty<TYPE> property = element.subtypeProperty();
		Map<TYPE, Icon> map = property.createMap();
		for (TYPE type : property.values()) {
			String name = base + type.subtypeName();
			Icon icon = register.registerIcon(name);
			map.put(type, icon);
		}
		return map;
	}

	private Icons() {
	}

}
