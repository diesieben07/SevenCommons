package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.internal.transformers.FieldAdder;
import gnu.trove.list.array.TLongArrayList;

/**
 * @author diesieben07
 */
public interface EntityPlayerMPProxy {

    @FieldAdder.FieldGetter(field = "_sc$chunks", creator = "createList")
    TLongArrayList _sc$viewedChunks();

    static TLongArrayList createList() {
        return new TLongArrayList();
    }

}
