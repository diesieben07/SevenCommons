package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.Packet;
import de.take_weiland.mods.commons.net.PacketHandlerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;

import java.util.concurrent.CompletionStage;

/**
 * @author diesieben07
 */
public interface PacketHandlerBaseWithResponse<P extends Packet.WithResponse<R>, R extends Packet.Response> extends PacketHandlerBase {

    @Deprecated
    void _sc$internal$handleInto(P packet, AcceptingCompletableFuture<R> future, byte side, NetworkManager manager);

    interface Plain<P extends Packet.WithResponse<R>, R extends Packet.Response> extends PacketHandlerBaseWithResponse<P, R> {

        R handle(P packet, EntityPlayer player);

        @Override
        @Deprecated
        default void _sc$internal$handleInto(P packet, AcceptingCompletableFuture<R> future, byte side, NetworkManager manager) {
            future.complete(handle(packet, NetworkImpl.getPlayer(side, manager)));
        }

    }

    interface Future<P extends Packet.WithResponse<R>, R extends Packet.Response> extends PacketHandlerBaseWithResponse<P, R> {

        CompletionStage<? extends R> handle(P packet, EntityPlayer player);

        @Override
        @Deprecated
        default void _sc$internal$handleInto(P packet, AcceptingCompletableFuture<R> future, byte side, NetworkManager manager) {
            handle(packet, NetworkImpl.getPlayer(side, manager)).whenComplete(future);
        }
    }

}
