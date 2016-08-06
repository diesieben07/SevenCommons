package de.take_weiland.mods.commons.util;

import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
