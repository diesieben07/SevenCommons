package de.take_weiland.mods.commons.internal.net;

import net.minecraft.entity.player.EntityPlayer;

import java.util.function.BiConsumer;

/**
 * @author diesieben07
 */
public final class SimplePacketData<P extends BaseModPacket> {

    final String channel;
    final int packetID;
    final BiConsumer<? super P, ? super EntityPlayer> handler;

    public SimplePacketData(String channel, int packetID, BiConsumer<? super P, ? super EntityPlayer> handler) {
        this.channel = channel;
        this.packetID = packetID;
        this.handler = handler;
    }

}
