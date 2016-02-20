package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.internal.transformers.FieldAdder;
import gnu.trove.set.hash.TLongHashSet;

/**
 * @author diesieben07
 */
public interface EntityPlayerMPProxy {

    @FieldAdder.FieldGetter(field = "_sc$chunks", creator = "createList")
    TLongHashSet _sc$viewedChunks();

    static TLongHashSet createList() {
        return new TLongHashSet();
    }

}
