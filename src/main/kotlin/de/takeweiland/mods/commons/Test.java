package de.takeweiland.mods.commons;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

/**
 * @author Take Weiland
 */
public class Test {

    static <T extends Comparable<T>> IBlockState applyPropertyValue(IBlockState state, IProperty<T> property, String rawValue) {
        return property.parseValue(rawValue).transform(v -> state.withProperty(property, v)).or(state);
    }



}
