package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;

/**
 * <p>Support for syncing of a Type {@code V}.</p>
 * <p>An optional companion type may be specified to store additional data to enable syncing.</p>
 *
 * @author diesieben07
 */
public interface Syncer<T_DATA> {

    Change<T_DATA> check();

    void encode(T_DATA data, MCDataOutput out);

    void apply(T_DATA data);

    void apply(MCDataInput in);

    interface Change<T_DATA> {

        static <T_DATA> Change<T_DATA> noChange() {
            return null;
        }

        static <T_DATA> Change<T_DATA> newValue(T_DATA data) {
            return null;
        }

    }

}
