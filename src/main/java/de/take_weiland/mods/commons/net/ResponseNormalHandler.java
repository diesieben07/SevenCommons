package de.take_weiland.mods.commons.net;

import net.minecraft.entity.player.EntityPlayer;

import java.util.function.BiFunction;

/**
 * <p>Handler for immediate responses (not wrapped in CompletionStage)</p>
 */
final class ResponseNormalHandler<P extends Packet.WithResponse<R>, R extends Packet.Response> extends ResponseHandler<P, R> {

    final BiFunction<? super P, ? super EntityPlayer, ? extends R> handler;

    ResponseNormalHandler(boolean async, PacketConstructor<P> constructor, PacketConstructor<R> responseConstructor, BiFunction<? super P, ? super EntityPlayer, ? extends R> handler) {
        super(async, constructor, responseConstructor);
        this.handler = handler;
    }

    @Override
    void doHandle(EntityPlayer player, int packetID, String channel, int responseID, P packet) {
        R response = handler.apply(packet, player);
        SimpleChannelBuilderImpl.sendResponse(response, player, packetID, responseID, channel);
    }
}
