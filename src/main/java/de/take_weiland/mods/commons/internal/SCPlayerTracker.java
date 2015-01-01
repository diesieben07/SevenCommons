package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.common.IPlayerTracker;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author diesieben07
 */
public final class SCPlayerTracker implements IPlayerTracker {

    @Override
    public void onPlayerLogin(EntityPlayer player) {
        PacketSyncPropsIDs.sendToIfNeeded(player, player);
//        if (!Players.isSPOwner(player)) {
//            Types.sendTypeIDs(player);
//        }
    }

    @Override
    public void onPlayerRespawn(EntityPlayer entityPlayer) { }

    @Override
    public void onPlayerChangedDimension(EntityPlayer entityPlayer) { }

    @Override
    public void onPlayerLogout(EntityPlayer entityPlayer) { }
}
