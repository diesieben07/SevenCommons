package de.take_weiland.mods.commons.net;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraft.entity.player.EntityPlayer;

import java.util.function.BiConsumer;

/**
 * <p>A base interface for a packet handler. Instances of this interface act as a {@code BiConsumer<P, EntityPlayer}.</p>
 *
 * @author diesieben07
 */
@FunctionalInterface
public interface SimplePacketHandler<P> extends BiConsumer<P, EntityPlayer> {

    /**
     * <p>Called when a packet is received.</p>
     * <p>On the client the client player will be passed, on the server the player sending the packet will be passed.</p>
     * @param packet the packet
     * @param player the player
     * @param side the logical side
     */
    void handle(P packet, EntityPlayer player, Side side);

    @Override
    default void accept(P packet, EntityPlayer player) {
        handle(packet, player, Sides.logical(player));
    }
}
