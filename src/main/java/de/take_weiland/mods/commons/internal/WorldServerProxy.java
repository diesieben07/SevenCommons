package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.internal.transformers.FieldAdder;

/**
 * @author diesieben07
 */
public interface WorldServerProxy {

    @FieldAdder.FieldGetter(field = "_sc$changedChunks", setter = "_sc$setChangedChunks")
    long[] _sc$getChangedChunks();

    void _sc$setChangedChunks(long[] arr);


}
