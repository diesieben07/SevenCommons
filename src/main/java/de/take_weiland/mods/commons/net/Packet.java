package de.take_weiland.mods.commons.net;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.net.BaseModPacket;
import net.minecraft.entity.player.EntityPlayerMP;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author diesieben07
 */
public interface Packet extends BaseModPacket, SimplePacket {

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

    interface WithResponse<R extends Packet> extends BaseModPacket, SimplePacket.WithResponse<R> {

        void writeTo(MCDataOutput out);

        @Override
        default CompletableFuture<R> sendToServer() {
            return null;
        }

        @Override
        default CompletableFuture<R> sendTo(EntityPlayerMP player) {
            return null;
        }

        @Override
        default void _sc$writeTo(MCDataOutput out) {

        }
    }

    @Override
    default void _sc$writeTo(MCDataOutput out) {
        writeTo(out);
    }
}
