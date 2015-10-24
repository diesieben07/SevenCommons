package de.take_weiland.mods.commons.internal;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;

/**
 * @author diesieben07
 */
public interface PlayerAwareInventory {

    void _sc$onPlayerViewContainer(Container container, int index, EntityPlayerMP player);

}
