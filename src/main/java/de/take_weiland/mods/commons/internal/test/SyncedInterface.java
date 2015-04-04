package de.take_weiland.mods.commons.internal.test;

import de.take_weiland.mods.commons.nbt.ToNbt;
import de.take_weiland.mods.commons.sync.Sync;

/**
 * @author diesieben07
 */
interface SyncedInterface {

    @Sync @ToNbt
    String getSomeData();

    void setSomeData(String i);

}
