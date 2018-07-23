package de.takeweiland.mods.commons;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/**
 * @author Take Weiland
 */
public class Test {

    static <T extends Comparable<T>> IBlockState applyPropertyValue(IBlockState state, IProperty<T> property, String rawValue) {
        return property.parseValue(rawValue).transform(v -> state.withProperty(property, v)).or(state);
    }

    private static ItemStack foo = new ItemStack((Block) null);

    public static void main(String[] args) {
        System.out.println(foo.equals(Items.ACACIA_BOAT));
    }



}
