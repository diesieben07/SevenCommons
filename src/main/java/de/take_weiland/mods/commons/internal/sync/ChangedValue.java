package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.sync.Syncer;

/**
 * @author diesieben07
 */
public final class ChangedValue<T_DATA> implements Syncer.Change<T_DATA> {

    private static final Object NULL = new Object();

    public int fieldId;
    public final Syncer<?, T_DATA, ?> syncer;
    public final T_DATA data;

    public ChangedValue(Syncer<?, T_DATA, ?> syncer, T_DATA data) {
        this.syncer = syncer;
        this.data = data;
    }

    public void writeData(MCDataOutput out) {
        syncer.write(data, out);
    }

}
