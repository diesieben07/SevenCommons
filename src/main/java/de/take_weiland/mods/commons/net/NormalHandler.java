package de.take_weiland.mods.commons.net;

import net.minecraft.entity.player.EntityPlayer;

/**
 * <p>Handler for normal packets.</p>
 */
class NormalHandler<P extends Packet> extends SimpleChannelBuilderImpl.Handler {

    private final PacketHandler<? super P> handler;
    private final PacketConstructor<P> constructor;

    NormalHandler(byte info, PacketHandler<? super P> handler, PacketConstructor<P> constructor) {
        super(info);
        this.handler = handler;
        this.constructor = constructor;
    }

    @Override
    public void accept(String channel, int packetID, MCDataInput in, EntityPlayer player) {
        handler.handle(constructor.construct(in), player);
    }
}
