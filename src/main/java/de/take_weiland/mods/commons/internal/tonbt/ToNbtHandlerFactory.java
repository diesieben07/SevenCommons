package de.take_weiland.mods.commons.internal.tonbt;

/**
 * @author diesieben07
 */
interface ToNbtHandlerFactory {

    ToNbtHandler getHandler(Class<?> clazz);

}
