package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.internal.transformers.FieldAdder;

/**
 * @author diesieben07
 */
public interface ChunkProxy {

    @FieldAdder.FieldGetter(field = "_sc$blockupdates", setter = "_sc$setBlockUpdates")
    int _sc$getBlockUpdates();

    void _sc$setBlockUpdates(int i);

}
