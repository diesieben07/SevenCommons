package de.take_weiland.mods.commons.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.take_weiland.mods.commons.meta.HasSubtypes;
import de.take_weiland.mods.commons.meta.MetadataProperty;
import de.take_weiland.mods.commons.meta.Subtype;
import de.take_weiland.mods.commons.util.SCReflector;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.Item;
import net.minecraft.util.Icon;

import java.util.Map;

@SideOnly(Side.CLIENT)
public final class Icons {

    public static <TYPE extends Subtype, ITEM extends Item & HasSubtypes<TYPE>> Map<TYPE, Icon> registerMulti(ITEM item, IconRegister register) {
        MetadataProperty<TYPE> property = item.subtypeProperty();
        Map<TYPE, Icon> map = property.createMap();
        for (TYPE type : property.values()) {
            String name = SCReflector.instance.getIconName(item) + "." + type.subtypeName();
            Icon icon = register.registerIcon(name);
            map.put(type, icon);
        }
        return map;
    }

	private Icons() { }
	
}
