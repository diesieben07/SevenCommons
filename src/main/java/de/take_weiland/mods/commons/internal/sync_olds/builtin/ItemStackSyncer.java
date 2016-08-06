package de.take_weiland.mods.commons.internal.sync_olds.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import de.take_weiland.mods.commons.sync.TypeSyncer;
import de.take_weiland.mods.commons.util.ItemStacks;
import net.minecraft.item.ItemStack;

/**
 * @author diesieben07
 */
enum ItemStackSyncer implements TypeSyncer<ItemStack, ItemStack, ItemStack> {

    INSTANCE;

    @Override
    public Change<ItemStack> check(Object obj, PropertyAccess<ItemStack> property, Object cObj, PropertyAccess<ItemStack> companion) {
        ItemStack value = property.get(obj);
        if (ItemStacks.identical(value, companion.get(cObj))) {
            return noChange();
        } else {
            ItemStack clone = ItemStacks.clone(value);
            companion.set(obj, clone);
            return newValue(clone);
        }
    }

    @Override
    public Change<ItemStack> forceUpdate(Object obj, PropertyAccess<ItemStack> property, Object cObj, PropertyAccess<ItemStack> companion) {
        return newValue(ItemStacks.clone(property.get(obj)));
    }


    @Override
    public void encode(ItemStack itemStack, MCDataOutput out) {
        out.writeItemStack(itemStack);
    }

    @Override
    public void apply(ItemStack itemStack, Object obj, PropertyAccess<ItemStack> property, Object cObj, PropertyAccess<ItemStack> companion) {
        property.set(obj, ItemStacks.clone(itemStack));
    }

    @Override
    public void apply(MCDataInput in, Object obj, PropertyAccess<ItemStack> property, Object cObj, PropertyAccess<ItemStack> companion) {
        property.set(obj, in.readItemStack());
    }

    @Override
    public Class<ItemStack> companionType() {
        return ItemStack.class;
    }
}
