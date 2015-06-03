package de.take_weiland.mods.commons.async;

/**
 * @author diesieben07
 */
public interface FunctionX<T, R> {

    R apply(T t) throws Exception;

}
