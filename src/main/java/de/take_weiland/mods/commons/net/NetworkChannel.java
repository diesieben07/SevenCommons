package de.take_weiland.mods.commons.net;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.function.Predicate;

/**
 * @author diesieben07
 */
public interface NetworkChannel<P> {

    void sendToServer(P packet);

    void sendTo(P packet, EntityPlayer player);

    void sendTo(P packet, Iterable<? extends EntityPlayer> players);

    void sendTo(P packet, Iterable<? extends EntityPlayer> players, Predicate<? super EntityPlayerMP> filter);

}
