package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.*;
import net.minecraft.network.NetworkManager;

/**
 * <p>Handler for normal packets.</p>
 */
final class NormalHandler<P extends Packet> extends SimpleChannelBuilderImpl.Handler {

    private final PacketHandler<? super P> handler;
    private final PacketConstructor<P> constructor;

    NormalHandler(byte characteristics, PacketHandler<? super P> handler, PacketConstructor<P> constructor) {
        super(characteristics);
        this.handler = handler;
        this.constructor = constructor;
    }

    @Override
    public void accept(String channel, int packetID, MCDataInput in, byte side, NetworkManager manager) {
        P packet;
        try {
            packet = constructor.newInstance(in);
        } catch (Exception e) {
            throw wrapConstructException(packetID, e);
        }
        if ((characteristics & Network.ASYNC) == 0) {
            NetworkImpl.getScheduler(side).execute(() -> {
                handler.handle(packet, NetworkImpl.getPlayer(side, manager));
                return false;
            });
        }
        handler.handle(packet, NetworkImpl.getPlayer(side, manager));
    }

    @Override
    public String toString() {
        return String.format("Normal packet handler (handler=%s, constructor=%s)", handler, constructor);
    }
}
