package de.take_weiland.mods.commons.util;

import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
class GuiIdentifierBuilderImpl implements GuiIdentifier.Builder {

    static final MethodHandle builderConstructor;

    static {
        try {
            Class<?> clazz = FMLLaunchHandler.side().isClient() ? Class.forName("de.take_weiland.mods.commons.util.GuiIdentifierBuilderImplClient") : GuiIdentifierBuilderImpl.class;
            builderConstructor = MethodHandles.lookup().findConstructor(clazz, methodType(void.class)).asType(methodType(GuiIdentifier.Builder.class));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    GuiIdentifierBuilderImpl() {

    }

    String modId;
    final Map<Integer, ContainerConstructorInternal> containerConstructors = new HashMap<>();

    // for unchecked warnings: ASM is used to add GuiConstructorInternal to GuiConstructor & GuiContainerConstructor, so this is safe
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <C extends Container> GuiIdentifier.Builder add(GuiIdentifier id, GuiIdentifier.ContainerConstructor<C> containerConstructor, Supplier<GuiIdentifier.GuiContainerConstructor<? extends C>> guiConstructor) {
        doAdd(id, containerConstructor, (Supplier) guiConstructor);
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public GuiIdentifier.Builder add(GuiIdentifier id, Supplier<GuiIdentifier.GuiConstructor> guiConstructor) {
        doAdd(id, (player, world, x, y, z) -> null, (Supplier) guiConstructor);
        return this;
    }

    void doAdd(GuiIdentifier id, ContainerConstructorInternal containerConstructor, Supplier<? extends GuiConstructorInternal> guiConstructor) {
        Object mod = id.mod();
        ModContainer container = FMLCommonHandler.instance().findContainerFor(mod);
        if (container == null) {
            throw new IllegalArgumentException(String.format("%s is not a known mod", mod));
        }

        if (modId == null) {
            modId = container.getModId();
        } else if (!modId.equals(container.getModId())) {
            throw new IllegalArgumentException(String.format("GuiIdentifiers registered to the same builder must return the same mod object, found %s and %s", mod, id.mod()));
        }

        if (containerConstructors.putIfAbsent(id.ordinal(), containerConstructor) != null) {
            throw new IllegalArgumentException(String.format("Duplicate GuiIdentifier %d", id.ordinal()));
        }
    }

    @Override
    public final void done() {
        if (containerConstructors.isEmpty()) {
            throw new IllegalArgumentException("Cannot create empty GuiHandler!");
        }
        NetworkRegistry.INSTANCE.registerGuiHandler(modId, createHandler());
    }

    IGuiHandler createHandler() {
        return new GuiHandlerImpl(modId, this.buildIndex(containerConstructors, ContainerConstructorInternal.class));
    }

    final <T> T[] buildIndex(Map<Integer, T> map, Class<T> clazz) {
        int maxIndex = Collections.max(map.keySet());

        @SuppressWarnings("unchecked")
        T[] arr = (T[]) Array.newInstance(clazz, maxIndex + 1);
        for (Map.Entry<Integer, T> entry : map.entrySet()) {
            arr[entry.getKey()] = entry.getValue();
        }

        return arr;
    }
}
