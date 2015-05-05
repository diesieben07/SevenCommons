package de.take_weiland.mods.commons.internal.sync.builtin;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.Syncer;
import net.minecraft.item.Item;

import java.util.function.Consumer;

/**
 * @author diesieben07
 */
enum ItemSyncer implements Syncer.Simple<Item, Item> {

    INSTANCE;

    @Override
    public Class<Item> getCompanionType() {
        return Item.class;
    }

    @Override
    public <T_OBJ> Change<Item, Item> checkChange(T_OBJ obj, Item value, Item companion, Consumer<Item> companionSetter) {
        if (value == companion) {
            return noChange();
        } else {
            companionSetter.accept(value);
            return newValue(value);
        }
    }

    @Override
    public void write(Item value, MCDataOutput out) {
        out.writeItem(value);
    }

    @Override
    public Item read(MCDataInput in) {
        return in.readItem();
    }
}
