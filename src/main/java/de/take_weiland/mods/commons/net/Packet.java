package de.take_weiland.mods.commons.net;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.SevenCommons;
import de.take_weiland.mods.commons.internal.net.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S3FPacketCustomPayload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * @author diesieben07
 */
public interface Packet extends BaseModPacket, BaseNettyPacket, SimplePacket {

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

    @Override
    default void _sc$handle(EntityPlayer player) {
        PacketToChannelMap.getData(this).handler.accept(this, player);
    }

    @Override
    default net.minecraft.network.Packet _sc$encodeToPlayer(EntityPlayerMP player) {
        return makePacket(this, S3FPacketCustomPayload::new);
    }

    @Override
    default net.minecraft.network.Packet _sc$encode() {
        return makePacket(this, SevenCommons.proxy.getC17PacketCstr());
    }

    static <P extends Packet> net.minecraft.network.Packet makePacket(P self, BiFunction<String, byte[], ? extends net.minecraft.network.Packet> constructor) {
        SimplePacketData<P> data = PacketToChannelMap.getData(self);
        MCDataOutput out = Network.newOutput(self.expectedSize() + 1);
        out.writeByte(data.packetID);
        self.writeTo(out);
        return constructor.apply(data.channel, out.toByteArray());
    }

    interface WithResponse<R extends Packet> extends SimplePacket.WithResponse<R>, BaseModPacket {

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

}
