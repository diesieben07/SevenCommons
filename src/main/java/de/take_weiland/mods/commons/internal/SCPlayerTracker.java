package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.common.IPlayerTracker;
import de.take_weiland.mods.commons.properties.Types;
import de.take_weiland.mods.commons.util.Players;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author diesieben07
 */
public final class SCPlayerTracker implements IPlayerTracker {

    @Override
    public void onPlayerLogin(EntityPlayer player) {
        if (!Players.isSPOwner(player)) {
            Types.sendTypeIDs(player);
        }
    }

    @Override
    public void onPlayerRespawn(EntityPlayer entityPlayer) { }

    @Override
    public void onPlayerChangedDimension(EntityPlayer entityPlayer) { }

    @Override
    public void onPlayerLogout(EntityPlayer entityPlayer) { }
}
