package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.Syncer;
import de.take_weiland.mods.commons.util.ItemStacks;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;

/**
 * @author diesieben07
 */
enum ItemStackSyncer implements Syncer.Simple<ItemStack, ItemStack> {

    INSTANCE;

    @Override
    public Class<ItemStack> getCompanionType() {
        return ItemStack.class;
    }

    @Override
    public <T_OBJ> Change<ItemStack> checkChange(T_OBJ obj, ItemStack value, ItemStack companion, Consumer<ItemStack> companionSetter) {
        if (ItemStacks.identical(value, companion)) {
            return noChange();
        } else {
            ItemStack clone = ItemStacks.clone(value);
            companionSetter.accept(clone);
            return newValue(clone);
        }
    }

    @Override
    public void write(ItemStack value, MCDataOutput out) {
        out.writeItemStack(value);
    }

    @Override
    public ItemStack read(MCDataInput in) {
        return in.readItemStack();
    }

}
