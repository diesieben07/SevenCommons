package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.internal.net.ResponseSupport;
import net.minecraft.entity.player.EntityPlayer;

import java.util.concurrent.CompletableFuture;

/**
 * <p>Base class for handlers that have a response of some form</p>
 */
abstract class ResponseHandler<P extends Packet.WithResponse<R>, R extends Packet.Response> extends SimpleChannelBuilderImpl.Handler {

    final PacketConstructor<P> constructor;
    final PacketConstructor<R> responseConstructor;

    ResponseHandler(byte info, PacketConstructor<P> constructor, PacketConstructor<R> responseConstructor) {
        super(info);
        this.constructor = constructor;
        this.responseConstructor = responseConstructor;
    }

    @Override
    public final void accept(String channel, int packetID, MCDataInput in, EntityPlayer player) {
        int uniqueID = in.readInt();

        if (ResponseSupport.isResponse(uniqueID)) {
            @SuppressWarnings("unchecked")
            CompletableFuture<R> future = (CompletableFuture<R>) ResponseSupport.get(uniqueID);

            if (future != null) {
                try {
                    future.complete(responseConstructor.apply(in));
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                }
            }
        } else {
            P packet = constructor.apply(in);
            doHandle(player, packetID, channel, uniqueID, packet);
        }
    }

    abstract void doHandle(EntityPlayer player, int packetID, String channel, int responseID, P packet);

}
