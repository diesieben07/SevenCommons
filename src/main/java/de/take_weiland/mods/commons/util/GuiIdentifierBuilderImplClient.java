package de.take_weiland.mods.commons.util;

import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author diesieben07
 */
@SideOnly(Side.CLIENT)
final class GuiIdentifierBuilderImplClient extends GuiIdentifierBuilderImpl {

    private final Map<Integer, GuiConstructorInternal> guiConstructors = new HashMap<>();

    @Override
    IGuiHandler createHandler() {
        return new GuiHandlerImplClient(modId, buildIndex(containerConstructors, ContainerConstructorInternal.class), buildIndex(guiConstructors, GuiConstructorInternal.class));
    }

    @Override
    void doAdd(GuiIdentifier id, ContainerConstructorInternal containerConstructor, Supplier<? extends GuiConstructorInternal> guiConstructor) {
        super.doAdd(id, containerConstructor, guiConstructor);
        guiConstructors.put(id.ordinal(), guiConstructor.get());
    }
}
