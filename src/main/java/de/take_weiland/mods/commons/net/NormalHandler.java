package de.take_weiland.mods.commons.net;

import net.minecraft.entity.player.EntityPlayer;

import java.util.function.BiConsumer;

/**
 * <p>Handler for normal packets.</p>
 */
class NormalHandler<P extends Packet> extends SimpleChannelBuilderImpl.Handler {

    private final BiConsumer<? super P, ? super EntityPlayer> handlerF;
    private final PacketConstructor<P> constructor;

    NormalHandler(boolean async, BiConsumer<? super P, ? super EntityPlayer> handlerF, PacketConstructor<P> constructor) {
        super(async);
        this.handlerF = handlerF;
        this.constructor = constructor;
    }

    @Override
    public void accept(String channel, int packetID, MCDataInput in, EntityPlayer player) {
        handlerF.accept(constructor.apply(in), player);
    }
}
