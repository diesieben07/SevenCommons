package de.take_weiland.mods.commons.client.icon;

import com.google.common.collect.ImmutableMap;
import de.take_weiland.mods.commons.internal.SCReflector;
import de.take_weiland.mods.commons.meta.HasSubtypes;
import de.take_weiland.mods.commons.meta.MetadataProperty;
import de.take_weiland.mods.commons.meta.Subtype;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * <p>Utilities for registering Icons.</p>
 */
public final class Icons {

    /**
     * <p>Register an Icon for each of the Item's subtypes.</p>
     *
     * @param item     the Item
     * @param register the icon registry
     * @return a Map mapping Subtypes to Icons
     */
    public static <TYPE extends Subtype, ITEM extends Item & HasSubtypes<TYPE>> Map<TYPE, IIcon> registerMulti(ITEM item, IIconRegister register) {
        return registerMulti0(SCReflector.instance.getIconName(item) + ".", item, register);
    }

    /**
     * <p>Register an Icon for each of the Block's subtypes.</p>
     *
     * @param block    the Block
     * @param register the icon registry
     * @return a Map mapping Subtypes to Icons
     */
    public static <TYPE extends Subtype, BLOCK extends Block & HasSubtypes<TYPE>> Map<TYPE, IIcon> registerMulti(BLOCK block, IIconRegister register) {
        return registerMulti0(SCReflector.instance.getIconName(block) + ".", block, register);
    }

    /**
     * <p>Create a new {@link IconManagerBuilder}.</p>
     *
     * @param register the {@code IIconRegister} to use
     * @return a new {@code IconManagerBuilder}
     */
    public static IconManagerBuilder newBuilder(IIconRegister register) {
        return new BuilderImpl(register);
    }

    private static <TYPE extends Subtype> Map<TYPE, IIcon> registerMulti0(String base, HasSubtypes<TYPE> element, IIconRegister register) {
        MetadataProperty<TYPE> property = element.subtypeProperty();
        Map<TYPE, IIcon> map = property.createMap();
        for (TYPE type : property.values()) {
            String name = base + type.subtypeName();
            IIcon icon = register.registerIcon(name);
            map.put(type, icon);
        }
        return map instanceof EnumMap ? Collections.unmodifiableMap(map) : ImmutableMap.copyOf(map);
    }

    private Icons() {
    }

}
