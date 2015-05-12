package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.Syncer;
import net.minecraft.item.Item;

/**
 * @author diesieben07
 */
enum ItemSyncer implements Syncer.ForImmutable<Item> {

    INSTANCE;

    @Override
    public Item decode(MCDataInput in) {
        return in.readItem();
    }

    @Override
    public void encode(Item item, MCDataOutput out) {
        out.writeItem(item);
    }

    @Override
    public Class<Item> companionType() {
        return Item.class;
    }
}
