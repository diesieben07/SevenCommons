package de.take_weiland.mods.commons.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;

/**
 * @author diesieben07
 */
interface GuiConstructorInternal {

    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    default GuiScreen createScreen(Container container) {
        return ((GuiIdentifier.GuiContainerConstructor<Container>) this).newInstance(container);
    }

    interface OnSingleGui extends GuiConstructorInternal {

        @SideOnly(Side.CLIENT)
        @Override
        default GuiScreen createScreen(Container container) {
            return ((GuiIdentifier.GuiConstructor) this).newInstance();
        }
    }

}
