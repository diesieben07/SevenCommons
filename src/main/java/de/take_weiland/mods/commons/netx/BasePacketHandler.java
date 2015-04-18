package de.take_weiland.mods.commons.netx;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author diesieben07
 */
interface BasePacketHandler<P extends Packet> {

    Packet receive0(P packet, EntityPlayer player, Side side);

}
