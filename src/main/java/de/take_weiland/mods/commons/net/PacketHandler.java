package de.take_weiland.mods.commons.net;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author diesieben07
 */
public interface PacketHandler {

    void accept(byte[] data, EntityPlayer player, Side side);

}
