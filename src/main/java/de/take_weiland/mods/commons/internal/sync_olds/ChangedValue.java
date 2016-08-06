package de.take_weiland.mods.commons.internal.sync_olds;

import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.TypeSyncer;

/**
 * @author diesieben07
 */
public class ChangedValue<T_DATA> {

    public int fieldId;
    public final TypeSyncer<?, ?, T_DATA> syncer;
    public final T_DATA data;

    public ChangedValue(TypeSyncer<?, ?, T_DATA> syncer, T_DATA data) {
        this.syncer = syncer;
        this.data = data;
    }

    public void writeData(MCDataOutput out) {
        syncer.encode(data, out);
    }

}
