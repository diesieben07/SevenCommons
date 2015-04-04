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
        Class<? extends ToNbtHandler> hClass = handlerClasses.get(clazz);
        if (hClass == null) {
            hClass = new BytecodeEmittingHandlerGenerator(clazz).generateHandler();
            handlerClasses.put(clazz, hClass);
        }
        try {
            return hClass == null ? null : hClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException();
        }
    }


}
