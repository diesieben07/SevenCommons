package de.take_weiland.mods.commons.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
