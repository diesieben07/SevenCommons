package de.take_weiland.mods.commons.internal;

import net.minecraft.client.gui.GuiTextField;

import java.util.List;

/**
 * @author diesieben07
 */
public interface GuiScreenProxy {

    String GET = "_sc$textFields";
    String CLASS_NAME = "de/take_weiland/mods/commons/internal/GuiScreenProxy";

    List<GuiTextField> _sc$textFields();

}
