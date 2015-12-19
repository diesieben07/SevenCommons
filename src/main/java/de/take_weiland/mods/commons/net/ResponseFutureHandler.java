package de.take_weiland.mods.commons.net;

import net.minecraft.entity.player.EntityPlayer;

import java.util.concurrent.CompletionStage;

/**
 * <p>Handler for responses wrapped in a CompletionStage&lt;R&gt;</p>
 */
final class ResponseFutureHandler<P extends Packet.WithResponse<R>, R extends Packet.Response> extends ResponseHandler<P, R> {

    final PacketHandler.WithAsyncResponse<? super P, ? extends R> handler;

    ResponseFutureHandler(byte info, PacketConstructor<P> constructor, PacketConstructor<R> responseConstructor, PacketHandler.WithAsyncResponse<? super P, ? extends R> handler) {
        super(info, constructor, responseConstructor);
        this.handler = handler;
    }

    @Override
    void doHandle(EntityPlayer player, int packetID, String channel, int responseID, P packet) {
        CompletionStage<? extends R> responseFuture = handler.handle(packet, player);
        responseFuture.thenAccept(response -> SimpleChannelBuilderImpl.sendResponse(response, player, packetID, responseID, channel));
    }

    @Override
    Object handler() {
        return handler;
    }
}
