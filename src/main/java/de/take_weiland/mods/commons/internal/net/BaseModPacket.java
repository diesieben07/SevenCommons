package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.Network;

/**
 * @author diesieben07
 */
public interface BaseModPacket {

    default int expectedSize() {
        return Network.DEFAULT_EXPECTED_SIZE;
    }

    default SimplePacketData _sc$getData() {
        return PacketToChannelMap.getDataFallback(this);
    }

}
