package de.take_weiland.mods.commons.net;

import net.minecraft.entity.player.EntityPlayer;

import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

/**
 * <p>Handler for responses wrapped in a CompletionStage&lt;R&gt;</p>
 */
final class ResponseFutureHandler<P extends Packet.WithResponse<R>, R extends Packet.Response> extends ResponseHandler<P, R> {

    final BiFunction<? super P, ? super EntityPlayer, ? extends CompletionStage<? extends R>> handler;

    ResponseFutureHandler(boolean async, PacketConstructor<P> constructor, PacketConstructor<R> responseConstructor, BiFunction<? super P, ? super EntityPlayer, ? extends CompletionStage<? extends R>> handler) {
        super(async, constructor, responseConstructor);
        this.handler = handler;
    }

    @Override
    void doHandle(EntityPlayer player, int packetID, String channel, int responseID, P packet) {
        CompletionStage<? extends R> responseFuture = handler.apply(packet, player);
        responseFuture.thenAccept(response -> SimpleChannelBuilderImpl.sendResponse(response, player, packetID, responseID, channel));
    }
}
