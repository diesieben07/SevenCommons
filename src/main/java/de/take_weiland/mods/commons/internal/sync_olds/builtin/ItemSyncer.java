package de.take_weiland.mods.commons.internal.sync_olds.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.TypeSyncer;
import net.minecraft.item.Item;

/**
 * @author diesieben07
 */
enum ItemSyncer implements TypeSyncer.ForImmutable<Item> {

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
