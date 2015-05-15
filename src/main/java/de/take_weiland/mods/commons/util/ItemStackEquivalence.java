package de.take_weiland.mods.commons.util;

import com.google.common.base.Equivalence;
import net.minecraft.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class ItemStackEquivalence extends Equivalence<ItemStack> {

    public static ItemStackEquivalence instance() {
        return instance;
    }

    @Override
    protected boolean doEquivalent(ItemStack a, ItemStack b) {
        return ItemStacks.identical(a, b);
    }

    @Override
    protected int doHash(ItemStack stack) {
        return ItemStacks.hash(stack);
    }

    private static final ItemStackEquivalence instance = new ItemStackEquivalence();

    private ItemStackEquivalence() {
    }

}
