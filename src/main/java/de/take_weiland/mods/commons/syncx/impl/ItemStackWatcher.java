package de.take_weiland.mods.commons.syncx.impl;

import de.take_weiland.mods.commons.syncx.SyncableProperty;
import de.take_weiland.mods.commons.syncx.Watcher;
import de.take_weiland.mods.commons.util.ItemStacks;
import net.minecraft.item.ItemStack;

/**
 * @author diesieben07
 */
public enum ItemStackWatcher implements Watcher<ItemStack> {
    INSTANCE;

    @Override
    public void setup(SyncableProperty<? extends ItemStack> property) { }

    @Override
    public boolean hasChanged(SyncableProperty<? extends ItemStack> property) {
        return !ItemStacks.identical(property.get(), (ItemStack) property.getData());
    }

    @Override
    public void afterWrite(SyncableProperty<? extends ItemStack> property) {
        property.setData(ItemStacks.clone(property.get()));
    }

}
