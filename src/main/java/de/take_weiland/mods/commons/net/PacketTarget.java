package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.net.simple.SimplePacket;

/**
 * @author diesieben07
 */
public interface PacketTarget {

    void send(SimplePacket packet);

}
