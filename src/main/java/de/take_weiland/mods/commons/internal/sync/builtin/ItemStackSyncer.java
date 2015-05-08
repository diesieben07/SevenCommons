package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.AbstractSyncer;
import de.take_weiland.mods.commons.sync.PropertyAccess;
import de.take_weiland.mods.commons.util.ItemStacks;
import net.minecraft.item.ItemStack;

/**
 * @author diesieben07
 */
final class ItemStackSyncer extends AbstractSyncer.WithCompanion<ItemStack, ItemStack, ItemStack> {

    protected <OBJ> ItemStackSyncer(OBJ obj, PropertyAccess<OBJ, ItemStack> property) {
        super(obj, property);
    }

    @Override
    protected Change<ItemStack> check(ItemStack value) {
        if (ItemStacks.identical(value, companion)) {
            return noChange();
        } else {
            ItemStack clone = ItemStacks.clone(value);
            companion = clone;
            return newValue(clone);
        }
    }

    @Override
    public void encode(ItemStack value, MCDataOutput out) {
        out.writeItemStack(value);
    }

    @Override
    public void apply(ItemStack value) {
        set(value);
    }

    @Override
    public void apply(MCDataInput in) {
        set(in.readItemStack());
    }
}
