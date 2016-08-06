package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.Packet;
import de.take_weiland.mods.commons.net.PacketConstructor;
import de.take_weiland.mods.commons.net.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;

/**
 * <p>Handler for immediate responses (not wrapped in CompletionStage)</p>
 */
final class ResponseNormalHandler<P extends Packet.WithResponse<R>, R extends Packet.Response> extends ResponseHandler<P, R> {

    final PacketHandler.WithResponse<? super P, ? extends R> handler;

    ResponseNormalHandler(byte characteristics, PacketConstructor<P> constructor, PacketConstructor<R> responseConstructor, PacketHandler.WithResponse<? super P, ? extends R> handler) {
        super(characteristics, constructor, responseConstructor);
        this.handler = handler;
    }

    @Override
    void doHandle(NetworkManager manager, EntityPlayer player, int packetID, String channel, int responseID, P packet) {
        R response = handler.handle(packet, player);
        ResponseHandler.sendResponse(response, manager, packetID, responseID, channel);
    }

    @Override
    Object handler() {
        return handler;
    }
}
