package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.Syncer;
import de.take_weiland.mods.commons.util.ItemStacks;
import net.minecraft.item.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author diesieben07
 */
enum ItemStackSyncer implements Syncer.Simple<ItemStack, ItemStack, ItemStack> {
    INSTANCE;

    @Override
    public <OBJ> Change<ItemStack> check(ItemStack value, ItemStack companion, OBJ obj, BiConsumer<OBJ, ItemStack> setter, BiConsumer<OBJ, ItemStack> cSetter) {
        if (ItemStacks.identical(value, companion)) {
            return noChange();
        } else {
            ItemStack clone = ItemStacks.clone(value);
            cSetter.accept(obj, clone);
            return newValue(clone);
        }
    }

    @Override
    public void encode(ItemStack itemStack, MCDataOutput out) {
        out.writeItemStack(itemStack);
    }

    @Override
    public <OBJ> void apply(ItemStack itemStack, OBJ obj, Function<OBJ, ItemStack> getter, BiConsumer<OBJ, ItemStack> setter) {
        setter.accept(obj, ItemStacks.clone(itemStack));
    }

    @Override
    public <OBJ> void apply(MCDataInput in, OBJ obj, Function<OBJ, ItemStack> getter, BiConsumer<OBJ, ItemStack> setter) {
        setter.accept(obj, in.readItemStack());
    }

    @Override
    public Class<ItemStack> companionType() {
        return ItemStack.class;
    }
}
