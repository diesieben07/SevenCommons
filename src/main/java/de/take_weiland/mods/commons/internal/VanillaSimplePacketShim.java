package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.net.SimplePacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;

/**
 * @author diesieben07
 */
public interface VanillaSimplePacketShim extends SimplePacket {

    @Override
    default void sendToServer() {
        SevenCommons.proxy.getClientNetworkManager().scheduleOutboundPacket((Packet) this);
    }

    @Override
    default void sendTo(EntityPlayerMP player) {
        player.playerNetServerHandler.sendPacket((Packet) this);
    }

}
