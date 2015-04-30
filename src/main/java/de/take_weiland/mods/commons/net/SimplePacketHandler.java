package de.take_weiland.mods.commons.net;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.util.Sides;
import net.minecraft.entity.player.EntityPlayer;

import java.util.function.BiConsumer;

/**
 * @author diesieben07
 */
@FunctionalInterface
public interface SimplePacketHandler<P> extends BiConsumer<P, EntityPlayer> {

    void handle(P packet, EntityPlayer player, Side side);

    @Override
    default void accept(P packet, EntityPlayer player) {
        handle(packet, player, Sides.logical(player));
    }
}
