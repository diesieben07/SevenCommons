package de.take_weiland.mods.commons.net;

import net.minecraft.entity.player.EntityPlayer;

/**
 * <p>Handler for normal packets.</p>
 */
final class NormalHandler<P extends Packet> extends SimpleChannelBuilderImpl.Handler {

    private final PacketHandler<? super P> handler;
    private final PacketConstructor<P> constructor;

    NormalHandler(byte info, PacketHandler<? super P> handler, PacketConstructor<P> constructor) {
        super(info);
        this.handler = handler;
        this.constructor = constructor;
    }

    @Override
    public void accept(String channel, int packetID, MCDataInput in, EntityPlayer player) {
        P packet;
        try {
            packet = constructor.construct(in);
        } catch (Exception e) {
            throw wrapConstructException(packetID, e);
        }
        handler.handle(packet, player);
    }

    @Override
    public String toString() {
        return String.format("Normal packet handler (handler=%s, constructor=%s)", handler, constructor);
    }
}
