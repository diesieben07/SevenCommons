package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.SimpleSyncer;
import de.take_weiland.mods.commons.util.ItemStacks;
import net.minecraft.item.ItemStack;

/**
 * @author diesieben07
 */
final class ItemStackSyncer implements SimpleSyncer<ItemStack, ItemStack> {
    @Override
    public Class<ItemStack> getValueType() {
        return ItemStack.class;
    }

    @Override
    public Class<ItemStack> getCompanionType() {
        return ItemStack.class;
    }

    @Override
    public boolean equal(ItemStack value, ItemStack companion) {
        return ItemStacks.identical(value, companion);
    }

    @Override
    public ItemStack writeAndUpdate(ItemStack value, ItemStack companion, MCDataOutput out) {
        out.writeItemStack(value);
        return ItemStacks.clone(value);
    }

    @Override
    public ItemStack read(ItemStack oldValue, ItemStack companion, MCDataInput in) {
        return in.readItemStack();
    }
}
