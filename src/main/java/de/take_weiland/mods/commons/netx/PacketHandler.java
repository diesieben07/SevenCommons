package de.take_weiland.mods.commons.netx;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author diesieben07
 */
@FunctionalInterface
public interface PacketHandler<P extends Packet> {

    void onReceive(P packet, EntityPlayer player, Side side);

}
