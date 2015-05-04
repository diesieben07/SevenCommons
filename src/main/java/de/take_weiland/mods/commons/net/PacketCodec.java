package de.take_weiland.mods.commons.net;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.function.Predicate;

/**
 * @author diesieben07
 */
public interface PacketCodec<P> {

    byte[] encode(P packet);

    P decode(byte[] payload);

    void handle(P packet, EntityPlayer player);

    String channel();

    default boolean doCustomLocalHandling(P packet, EntityPlayer player) {
       return false;
    }

    default void sendToServer(P packet) {
        Network.sendToServer(packet, this);
    }

    default void sendTo(P packet, EntityPlayer player) {
        Network.sendTo(this, packet, player);
    }

    default void sendTo(P packet, Iterable<? extends EntityPlayer> players) {
        Network.sendTo(this, packet, players);
    }

    default void sendTo(P packet, Iterable<? extends EntityPlayer> players, Predicate<? super EntityPlayerMP> filter) {
        Network.sendTo(this, packet, players, filter);
    }

}
