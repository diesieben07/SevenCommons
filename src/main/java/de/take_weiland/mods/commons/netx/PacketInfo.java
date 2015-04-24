package de.take_weiland.mods.commons.netx;

/**
 * @author diesieben07
 */
final class PacketInfo {

    final String channel;
    final int id;

    PacketInfo(String channel, int id) {
        this.channel = channel;
        this.id = id;
    }
}
