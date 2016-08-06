package de.take_weiland.mods.commons.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

/**
 * @author diesieben07
 */
class GuiHandlerImpl implements IGuiHandler {

    private final String                         modId;
    private final ContainerConstructorInternal[] containerConstructors;

    GuiHandlerImpl(String modId, ContainerConstructorInternal[] containerConstructors) {
        this.modId = modId;
        this.containerConstructors = containerConstructors;
    }

    @Override
    public Container getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        ContainerConstructorInternal constructor = JavaUtils.get(containerConstructors, ID);
        if (constructor == null) {
            throw makeUnknownIdException(ID, player, world, x, y, z);
        }
        return constructor.newInstance(player, world, x, y, z);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    final IllegalArgumentException makeUnknownIdException(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new IllegalArgumentException(String.format("Unknown GuiID %d for mod %s (opened for player %s in world %s, parameters are %d, %d, %d)", ID, modId, player, world, x, y, z));
    }
}
