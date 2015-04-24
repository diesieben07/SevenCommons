package de.take_weiland.mods.commons.client;

import de.take_weiland.mods.commons.meta.HasSubtypes;
import de.take_weiland.mods.commons.meta.MetadataProperty;
import de.take_weiland.mods.commons.meta.Subtype;
import de.take_weiland.mods.commons.util.SCReflector;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;

import java.util.Map;

public final class Icons {

	public static <TYPE extends Subtype, ITEM extends Item & HasSubtypes<TYPE>> Map<TYPE, IIcon> registerMulti(ITEM item, IIconRegister register) {
		return registerMulti0(SCReflector.instance.getIconName(item) + ".", item, register);
	}

	public static <TYPE extends Subtype, BLOCK extends Block & HasSubtypes<TYPE>> Map<TYPE, IIcon> registerMulti(BLOCK block, IIconRegister register) {
		return registerMulti0(SCReflector.instance.getIconName(block) + ".", block, register);
	}

	private static <TYPE extends Subtype> Map<TYPE, IIcon> registerMulti0(String base, HasSubtypes<TYPE> element, IIconRegister register) {
		MetadataProperty<TYPE> property = element.subtypeProperty();
		Map<TYPE, IIcon> map = property.createMap();
		for (TYPE type : property.values()) {
			String name = base + type.subtypeName();
			IIcon icon = register.registerIcon(name);
			map.put(type, icon);
		}
		return map;
	}

	private Icons() {
	}

}
