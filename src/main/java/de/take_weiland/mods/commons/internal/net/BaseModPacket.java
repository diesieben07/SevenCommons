package de.take_weiland.mods.commons.internal.net;

/**
 * @author diesieben07
 */
public interface BaseModPacket {

    String CLASS_NAME = "de/take_weiland/mods/commons/internal/net/BaseModPacket";

    default SimplePacketData _sc$getData() {
        return PacketToChannelMap.getDataFallback(this);
    }

}
