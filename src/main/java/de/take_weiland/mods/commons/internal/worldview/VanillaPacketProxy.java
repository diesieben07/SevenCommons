package de.take_weiland.mods.commons.internal.worldview;

import de.take_weiland.mods.commons.internal.transformers.FieldAdder;

/**
 * @author diesieben07
 */
public interface VanillaPacketProxy {

    @FieldAdder.FieldGetter(field = "_sc$dim", setter = "_sc$setTargetDimension", creator = "initDimId")
    int _sc$targetDimension();

    void _sc$setTargetDimension(int dim);

    static int initDimId() {
        return VanillaPacketPrefixes.NOOP_DIM_ID;
    }

}
