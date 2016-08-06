package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.Packet;
import de.take_weiland.mods.commons.net.PacketConstructor;
import de.take_weiland.mods.commons.net.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;

import java.util.concurrent.CompletionStage;

/**
 * <p>Handler for responses wrapped in a CompletionStage&lt;R&gt;</p>
 */
final class ResponseFutureHandler<P extends Packet.WithResponse<R>, R extends Packet.Response> extends ResponseHandler<P, R> {

    final PacketHandler.WithAsyncResponse<? super P, ? extends R> handler;

    ResponseFutureHandler(byte characteristics, PacketConstructor<P> constructor, PacketConstructor<R> responseConstructor, PacketHandler.WithAsyncResponse<? super P, ? extends R> handler) {
        super(characteristics, constructor, responseConstructor);
        this.handler = handler;
    }

    @Override
    void doHandle(NetworkManager manager, EntityPlayer player, int packetID, String channel, int responseID, P packet) {
        CompletionStage<? extends R> responseFuture = handler.handle(packet, player);
        responseFuture.thenAccept(response -> ResponseHandler.sendResponse(response, manager, packetID, responseID, channel));
    }

    @Override
    Object handler() {
        return handler;
    }
}
