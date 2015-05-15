package de.take_weiland.mods.commons.net;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.function.Predicate;

import static de.take_weiland.mods.commons.internal.net.PacketToChannelMap.getChannel;

/**
 * @author diesieben07
 */
public interface Packet extends SimplePacket {

    void writeTo(MCDataOutput out);

    default int expectedSize() {
        return Network.DEFAULT_EXPECTED_SIZE;
    }

    default Side receivingSide() {
        return Optional.ofNullable(getClass().getAnnotation(Receiver.class))
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
        getChannel(this).sendToServer(this);
    }

    @Override
    default void sendTo(EntityPlayer player) {
        getChannel(this).sendTo(this, player);
    }

    @Override
    default void sendTo(Iterable<? extends EntityPlayer> players) {
        getChannel(this).sendTo(this, players);
    }

    @Override
    default void sendTo(Iterable<? extends EntityPlayer> players, Predicate<? super EntityPlayerMP> filter) {
        getChannel(this).sendTo(this, players, filter);
    }
}
