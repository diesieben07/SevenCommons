package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.AbstractSyncer;
import de.take_weiland.mods.commons.sync.PropertyAccess;
import net.minecraft.item.Item;

/**
 * @author diesieben07
 */
final class ItemSyncer extends AbstractSyncer.ForImmutable<Item> {

    protected <OBJ> ItemSyncer(OBJ obj, PropertyAccess<OBJ, Item> property) {
        super(obj, property);
    }

    @Override
    protected Item decode(MCDataInput in) {
        return in.readItem();
    }

    @Override
    public void encode(Item item, MCDataOutput out) {
        out.writeItem(item);
    }
}
