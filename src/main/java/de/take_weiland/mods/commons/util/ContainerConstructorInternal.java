package de.take_weiland.mods.commons.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;

/**
 * @author diesieben07
 */
interface ContainerConstructorInternal {

    Container newInstance(EntityPlayer player, World world, int x, int y, int z);

}
