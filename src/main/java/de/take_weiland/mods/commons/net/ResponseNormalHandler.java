package de.take_weiland.mods.commons.net;

import net.minecraft.entity.player.EntityPlayer;

/**
 * <p>Handler for immediate responses (not wrapped in CompletionStage)</p>
 */
final class ResponseNormalHandler<P extends Packet.WithResponse<R>, R extends Packet.Response> extends ResponseHandler<P, R> {

    final PacketHandler.WithResponse<? super P, ? extends R> handler;

    ResponseNormalHandler(byte info, PacketConstructor<P> constructor, PacketConstructor<R> responseConstructor, PacketHandler.WithResponse<? super P, ? extends R> handler) {
        super(info, constructor, responseConstructor);
        this.handler = handler;
    }

    @Override
    void doHandle(EntityPlayer player, int packetID, String channel, int responseID, P packet) {
        R response = handler.handle(packet, player);
        SimpleChannelBuilderImpl.sendResponse(response, player, packetID, responseID, channel);
    }
}
