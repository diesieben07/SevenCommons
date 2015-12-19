package de.take_weiland.mods.commons.net;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.net.BaseNettyPacket;
import de.take_weiland.mods.commons.internal.net.NetworkImpl;
import de.take_weiland.mods.commons.internal.net.PacketAdditionalMethods;
import de.take_weiland.mods.commons.internal.net.WrappedPacketWithResponse;
import net.minecraft.entity.player.EntityPlayerMP;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * <p>A simple interface for implementing custom packets for your Mod. They will be registered using {@link Network#newSimpleChannel(String)}
 * and will be a sent using the vanilla custom payload packets.</p>
 * <p>An instance of {@code Packet} must never carry mutable state that is referenced outside of it unless it is ensured that the state
 * can be accessed from multiple threads concurrently.</p>
 * <p>If a packet is send through the local channel on SSP <i>the same instance</i> will be received on the other side.</p>
 * <p>{@code Packet} extends {@link SimplePacket}, the methods defined there are used to send this.</p>
 *
 * @see SimpleChannelBuilder
 * @see SimplePacket
 *
 * @author diesieben07
 */
public interface Packet extends SimplePacket, PacketBase {

    /**
     * <p>The {@code Async} annotation may be used on a class that implements either {@code Packet} or {@code Packet.WithResponse} and
     * will make any handler for that class execute on the netty thread instead of the the main game thread.</p>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Async {
    }

    /**
     * <p>The {@code Receiver} annotation may be used on a class that implements either {@code Packet} or {@code Packet.WithResponse} and
     * defines which logical side may receive this packet. No annotation implies that the Packet can be send both ways. If the packet is received on an invalid Side,
     * a {@link ProtocolException} will be thrown.</p>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Receiver {

        Side value();

    }

    @Override
    default void sendToServer() {
        /**
         * @see PacketAdditionalMethods
         */
        NetworkImpl.sendRawPacketToServer((BaseNettyPacket) this);
    }

    @Override
    default void sendTo(EntityPlayerMP player) {
        NetworkImpl.sendRawPacket(player, (BaseNettyPacket) this);
    }

    /**
     * <p>A version of {@code Packet} that has a response. The response class needs to implement {@link de.take_weiland.mods.commons.net.Packet.Response}.</p>
     * <p>The response is supplied in form of a {@link CompletionStage} when an instance of this packet is sent.</p>
     * <p>{@code Packet.WithResponse} extends {@link SimplePacket.WithResponse}, the methods defined there are used to send this packet.</p>
     *
     * @see SimplePacket.WithResponse
     */
    interface WithResponse<R extends Packet.Response> extends SimplePacket.WithResponse<R>, PacketBase {

        @Override
        default CompletionStage<R> sendToServer() {
            CompletableFuture<R> future = new CompletableFuture<>();
            NetworkImpl.sendRawPacketToServer(new WrappedPacketWithResponse<>(this, future));
            return future;
        }

        @Override
        default CompletionStage<R> sendTo(EntityPlayerMP player) {
            CompletableFuture<R> future = new CompletableFuture<>();
            NetworkImpl.sendRawPacket(player, new WrappedPacketWithResponse<>(this, future));
            return future;
        }

    }
    /**
     * <p>The response for a {@code Packet.WithResponse}.</p>
     */
    interface Response extends PacketBase {
    }
}
