package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.Network;

/**
 * @author diesieben07
 */
public interface BasePacket {

    void writeTo(MCDataOutput out);

    default int expectedSize() {
        return Network.DEFAULT_EXPECTED_SIZE;
    }

}
