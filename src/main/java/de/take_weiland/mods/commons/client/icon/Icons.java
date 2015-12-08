package de.take_weiland.mods.commons.client.icon;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import de.take_weiland.mods.commons.internal.SCReflector;
import de.take_weiland.mods.commons.meta.HasSubtypes;
import de.take_weiland.mods.commons.meta.MetadataProperty;
import de.take_weiland.mods.commons.meta.Subtype;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.util.RegistryNamespaced;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Utilities for registering Icons.</p>
 */
@ParametersAreNonnullByDefault
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
        return new BuilderImpl(register, null);
    }

    /**
     * <p>Create a new {@link IconManagerBuilder} which will inherit the default texture domain from the given Block.</p>
     *
     * @param register the {@code IIconRegister} to use
     * @param block    the Block
     * @return a new {@code IconManagerBuilder}
     */
    public static IconManagerBuilder newBuilder(IIconRegister register, Block block) {
        return doNewBuilder(register, "Block", Block.blockRegistry, block);
    }

    /**
     * <p>Create a new {@link IconManagerBuilder} which will inherit the default texture domain from the given Item.</p>
     *
     * @param register the {@code IIconRegister} to use
     * @param item     the Item
     * @return a new {@code IconManagerBuilder}
     */
    public static IconManagerBuilder newBuilder(IIconRegister register, Item item) {
        return doNewBuilder(register, "Item", Item.itemRegistry, item);
    }

    /**
     * <p>Create a new {@link IconManagerBuilder} which will use the given default texture domain.</p>
     *
     * @param register the {@code IIconRegister} to use
     * @param domain   the texture domain
     * @return a new {@code IconManagerBuilder}
     */
    public static IconManagerBuilder newBuilder(IIconRegister register, String domain) {
        return new BuilderImpl(register, domain);
    }

    private static final Splitter SPLIT_RESOURCE = Splitter.on(':');

    private static IconManagerBuilder doNewBuilder(IIconRegister register, String type, RegistryNamespaced registry, Object thing) {
        String thingName = registry.getNameForObject(thing);
        if (thingName == null) {
            throw new IllegalArgumentException(String.format("%s %s not registered", type, thing));
        }

        List<String> split = SPLIT_RESOURCE.splitToList(thingName);
        String domain;
        if (split.size() == 1) {
            domain = null;
        } else {
            domain = split.get(0);
        }
        return new BuilderImpl(register, domain);
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
