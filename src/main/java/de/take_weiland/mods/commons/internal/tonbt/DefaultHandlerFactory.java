package de.take_weiland.mods.commons.internal.tonbt;

import java.util.HashMap;
import java.util.Map;

/**
 * @author diesieben07
 */
final class DefaultHandlerFactory implements ToNbtHandlerFactory {

    private final Map<Class<?>, Class<? extends ToNbtHandler>> handlerClasses = new HashMap<>();

    @Override
    public ToNbtHandler getHandler(Class<?> clazz) {
        Class<? extends ToNbtHandler> hClass = getHandlerClass(clazz);
        try {
            return hClass == null ? null : hClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException();
        }
    }

    Class<? extends ToNbtHandler> getHandlerClass(Class<?> clazz) {
        if (clazz == Object.class) {
            return null;
        }

        Class<? extends ToNbtHandler> hClass;
        if (!handlerClasses.containsKey(clazz)) {
            hClass = new BytecodeEmittingHandlerGenerator(this, clazz).generateHandler();
            if (hClass == null) {
                hClass = getHandlerClass(clazz.getSuperclass());
            }
            handlerClasses.put(clazz, hClass);
        } else {
            hClass = handlerClasses.get(clazz);
        }
        return hClass;
    }

}
