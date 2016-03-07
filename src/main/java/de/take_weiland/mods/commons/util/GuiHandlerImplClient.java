package de.take_weiland.mods.commons.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * @author diesieben07
 */
@SideOnly(Side.CLIENT)
final class GuiHandlerImplClient extends GuiHandlerImpl {

    private final GuiConstructorInternal[] guiConstructors;

    GuiHandlerImplClient(String modId, ContainerConstructorInternal[] containerConstructors, GuiConstructorInternal[] guiConstructors) {
        super(modId, containerConstructors);
        this.guiConstructors = guiConstructors;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        GuiConstructorInternal constructor = JavaUtils.get(guiConstructors, ID);
        if (constructor == null) {
            throw makeUnknownIdException(ID, player, world, x, y, z);
        }
        return constructor.createScreen(getServerGuiElement(ID, player, world, x, y, z));
    }
}
