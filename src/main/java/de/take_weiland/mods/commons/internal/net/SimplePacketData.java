package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.Packet;
import net.minecraft.entity.player.EntityPlayer;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * @author diesieben07
 */
public abstract class SimplePacketData<P extends BaseModPacket> {

    public final String channel;
    public final int packetID;

    public SimplePacketData(String channel, int packetID) {
        this.channel = channel;
        this.packetID = packetID;
    }

    public static final class Normal<P extends Packet> extends SimplePacketData<P> {

        public final BiConsumer<? super P, ? super EntityPlayer> handler;

        public Normal(String channel, int packetID, BiConsumer<? super P, ? super EntityPlayer> handler) {
            super(channel, packetID);
            this.handler = handler;
        }
    }

    public static final class WithResponse<P extends Packet.WithResponse<R>, R extends Packet.Response> extends SimplePacketData<P> {

        final BiFunction<? super P, ? super EntityPlayer, ? extends R> handler;

        public WithResponse(String channel, int packetID, BiFunction<? super P, ? super EntityPlayer, ? extends R> handler) {
            super(channel, packetID);
            this.handler = handler;
        }

    }

}
