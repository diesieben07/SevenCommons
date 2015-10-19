package de.take_weiland.mods.commons.internal.net;

import de.take_weiland.mods.commons.net.Packet;
import de.take_weiland.mods.commons.net.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;

import java.util.concurrent.CompletableFuture;

/**
 * @author diesieben07
 */
public abstract class SimplePacketData {

    public final String channel;
    public final int packetID;
    public final byte info;

    public SimplePacketData(String channel, int packetID, byte info) {
        this.channel = channel;
        this.packetID = packetID;
        this.info = info;
    }

    public static final class Normal<P extends Packet> extends SimplePacketData {

        public final PacketHandler<? super P> handler;

        public Normal(String channel, int packetID, byte info, PacketHandler<? super P> handler) {
            super(channel, packetID, info);
            this.handler = handler;
        }

    }

    public static abstract class WithResponse<P extends Packet.WithResponse<R>, R extends Packet.Response> extends SimplePacketData {

        WithResponse(String channel, int packetID, byte info) {
            super(channel, packetID, info);
        }

        public abstract void completeFuture(CompletableFuture<R> future, P packet, EntityPlayer player);
    }

    public static final class WithResponseNormal<P extends Packet.WithResponse<R>, R extends Packet.Response> extends WithResponse<P, R> {

        private final PacketHandler.WithResponse<? super P, ? extends R> handler;

        public WithResponseNormal(String channel, int packetID, byte info, PacketHandler.WithResponse<? super P, ? extends R> handler) {
            super(channel, packetID, info);
            this.handler = handler;
        }

        @Override
        public void completeFuture(CompletableFuture<R> future, P packet, EntityPlayer player) {
            try {
                future.complete(handler.handle(packet, player));
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        }
    }

    public static final class WithResponseFuture<P extends Packet.WithResponse<R>, R extends Packet.Response> extends WithResponse<P, R> {

        private final PacketHandler.WithAsyncResponse<? super P, ? extends R> handler;

        public WithResponseFuture(String channel, int packetID, byte info, PacketHandler.WithAsyncResponse<? super P, ? extends R> handler) {
            super(channel, packetID, info);
            this.handler = handler;
        }

        @Override
        public void completeFuture(CompletableFuture<R> future, P packet, EntityPlayer player) {
            handler.handle(packet, player).whenComplete((r, x) -> {
                if (x != null) {
                    future.completeExceptionally(x);
                } else {
                    future.complete(r);
                }
            });
        }

    }

}
