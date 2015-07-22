package de.take_weiland.mods.commons.net;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.net.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * <p>A simple interface for implementing custom packets for your Mod. They will be registered using {@link Network#newSimpleChannel(String)}
 * and will be a sent using the vanilla custom payload packets.</p>
 * <p>An instance of {@code Packet} must never carry mutable state that is referenced outside of it unless it is ensured that the state
 * can be accessed from multiple threads concurrently.</p>
 * <p>If a packet is send through the local channel on SSP <i>the same instance</i> will be received on the other side.</p>
 *
 * @see SimpleChannelBuilder
 * @see SimplePacket
 *
 * @author diesieben07
 */
public interface Packet extends BaseModPacket, SimplePacket, BaseNettyPacket {

    /**
     * <p>Encode this packet to the output stream.</p>
     *
     * @param out the output stream
     */
    void writeTo(MCDataOutput out);

    static Side receivingSide(Class<? extends Packet> clazz) {
        return Optional.ofNullable(clazz.getAnnotation(Receiver.class))
                .map(Receiver::value)
                .orElseThrow(() -> new IllegalStateException("Packet missing @Receiver annotation"));
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Receiver {

        Side value();

    }

    @Override
    default void sendToServer() {
        Network.sendToServer(this);
    }

    @Override
    default void sendTo(EntityPlayerMP player) {
        Network.sendToPlayer(player, this);
    }

    /**
     * <p>A version of {@code Packet} that has a response. The response class needs to implement {@link de.take_weiland.mods.commons.net.Packet.Response}.</p>
     * <p>The response is supplied in form of a {@link CompletableFuture} when an instance of this packet is sent.</p>
     * @see de.take_weiland.mods.commons.net.SimplePacket.WithResponse
     */
    interface WithResponse<R extends Packet.Response> extends SimplePacket.WithResponse<R>, BaseModPacket {

        /**
         * <p>Write this packet's data to the output stream.</p>
         *
         * @param out the output stream
         */
        void writeTo(MCDataOutput out);

        @Override
        default CompletableFuture<R> sendToServer() {
            CompletableFuture<R> future = new CompletableFuture<>();
            Network.sendToServer(new ResponseNettyVersion<>(this, future));
            return future;
        }

        @Override
        default CompletableFuture<R> sendTo(EntityPlayerMP player) {
            CompletableFuture<R> future = new CompletableFuture<>();
            Network.sendToPlayer(player, new ResponseNettyVersion<>(this, future));
            return future;
        }
    }

    /**
     * <p>The response for a {@code Packet.WithResponse}.</p>
     */
    interface Response extends BaseModPacket {

        /**
         * <p>Write this packet's data to the output stream.</p>
         *
         * @param out the output stream
         */
        void writeTo(MCDataOutput out);

    }

    @Override
    default void _sc$handle(EntityPlayer player) {
        PacketToChannelMap.getData(this).handler.accept(this, player);
    }

    @Override
    default byte[] _sc$encode() {
        return NetworkImpl.encodePacket(this, PacketToChannelMap.getData(this));
    }

    @Override
    default String _sc$channel() {
        return PacketToChannelMap.getData(this).channel;
    }
}
