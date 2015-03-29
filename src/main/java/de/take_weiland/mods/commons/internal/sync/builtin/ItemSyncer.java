package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.Syncer;
import net.minecraft.item.Item;

/**
 * @author diesieben07
 */
final class ItemSyncer implements Syncer<Item, Item> {

    @Override
    public Class<Item> getCompanionType() {
        return Item.class;
    }

    @Override
    public boolean equal(Item value, Item companion) {
        return value == companion;
    }

    @Override
    public Item writeAndUpdate(Item value, Item companion, MCDataOutput out) {
        out.writeItem(value);
        return value;
    }

    @Override
    public Item read(Item oldValue, Item companion, MCDataInput in) {
        return in.readItem();
    }
}
