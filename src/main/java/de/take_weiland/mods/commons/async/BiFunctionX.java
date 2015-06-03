package de.take_weiland.mods.commons.async;

/**
 * @author diesieben07
 */
public interface BiFunctionX<T, U, R> {

    R apply(T t, U u) throws Exception;

}
