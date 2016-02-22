package de.take_weiland.mods.commons.internal;

import com.google.common.base.Throwables;
import de.take_weiland.mods.commons.asm.MCPNames;
import net.minecraft.block.Block;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTSizeTracker;

import java.io.DataInput;
import java.io.DataOutput;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static java.lang.invoke.MethodHandles.publicLookup;
import static java.lang.invoke.MethodType.methodType;

/**
 * @author diesieben07
 */
public final class CommonMethodHandles {

    public static final MethodHandle blockIconNameGet, blockUnlocalizedNameGet, itemIconNameGet, itemUnlocalizedNameGet,
            setHasSubtypes, containerAddSlot, containerGetCrafters, nbtBaseWrite, nbtBaseRead, newNbt, createSharedString,
            objectClone;

    static {
        try {
            Field field = Block.class.getDeclaredField(MCPNames.field(SRGConstants.F_TEXTURE_NAME_BLOCK));
            field.setAccessible(true);
            blockIconNameGet = publicLookup().unreflectGetter(field);

            field = Block.class.getDeclaredField(MCPNames.field(SRGConstants.F_UNLOCALIZED_NAME_BLOCK));
            field.setAccessible(true);
            blockUnlocalizedNameGet = publicLookup().unreflectGetter(field);

            field = Item.class.getDeclaredField(MCPNames.field(SRGConstants.F_ICON_STRING));
            field.setAccessible(true);
            itemIconNameGet = publicLookup().unreflectGetter(field);

            field = Item.class.getDeclaredField(MCPNames.field(SRGConstants.F_UNLOCALIZED_NAME_ITEM));
            field.setAccessible(true);
            itemUnlocalizedNameGet = publicLookup().unreflectGetter(field);

            Method method = Item.class.getDeclaredMethod(MCPNames.method(SRGConstants.M_SET_HAS_SUBTYPES), boolean.class);
            method.setAccessible(true);
            setHasSubtypes = publicLookup().unreflect(method).asType(methodType(void.class, Item.class, boolean.class));

            method = Container.class.getDeclaredMethod(MCPNames.method(SRGConstants.M_ADD_SLOT_TO_CONTAINER), Slot.class);
            method.setAccessible(true);
            containerAddSlot = publicLookup().unreflect(method).asType(methodType(void.class, Container.class, Slot.class));

            field = Container.class.getDeclaredField(MCPNames.field(SRGConstants.F_CRAFTERS));
            field.setAccessible(true);
            containerGetCrafters = publicLookup().unreflectGetter(field);

            method = NBTBase.class.getDeclaredMethod(MCPNames.method(SRGConstants.M_NBT_WRITE), DataOutput.class);
            method.setAccessible(true);
            nbtBaseWrite = publicLookup().unreflect(method);

            method = NBTBase.class.getDeclaredMethod(MCPNames.method(SRGConstants.M_NBT_LOAD), DataInput.class, int.class, NBTSizeTracker.class);
            method.setAccessible(true);
            nbtBaseRead = publicLookup().unreflect(method);

            method = NBTBase.class.getDeclaredMethod(MCPNames.method(SRGConstants.M_NEW_NBT_TAG), byte.class);
            method.setAccessible(true);
            newNbt = publicLookup().unreflect(method);

            Constructor<?> constructor;
            MethodHandle mh;
            try {
                constructor = String.class.getDeclaredConstructor(char[].class, boolean.class);
                constructor.setAccessible(true);
                mh = MethodHandles.insertArguments(publicLookup().unreflectConstructor(constructor), 1, true);
            } catch (NoSuchMethodException e) {
                // weird JDK without the sharing constructor
                constructor = String.class.getConstructor(char[].class);
                mh = publicLookup().unreflectConstructor(constructor);
            }
            createSharedString = mh;

            method = Object.class.getDeclaredMethod("clone");
            method.setAccessible(true);
            objectClone = publicLookup().unreflect(method);
        } catch (Throwable x) {
            throw Throwables.propagate(x);
        }
    }

    public static List<ICrafting> getListeners(Container container) {
        try {
            //noinspection unchecked
            return (List<ICrafting>) containerGetCrafters.invokeExact(container);
        } catch (Throwable x) {
            throw Throwables.propagate(x);
        }
    }

    private CommonMethodHandles() {
    }
}
