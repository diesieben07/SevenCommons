package de.take_weiland.mods.commons.util;

import com.google.common.base.Throwables;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @author diesieben07
 */
final class SCMethodHandles {

    private static final MethodHandle containerListenersMH;

    @SuppressWarnings("unchecked")
    static List<IContainerListener> getListeners(Container c) throws Throwable {
        return (List<IContainerListener>) containerListenersMH.invokeExact(c);
    }

    static {
        try {
            Field field = Container.class.getDeclaredField("listeners");
            field.setAccessible(true);
            containerListenersMH = MethodHandles.publicLookup().unreflectGetter(field);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw Throwables.propagate(e);
        }
    }

}
