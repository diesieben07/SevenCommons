package de.take_weiland.mods.commons.async;

/**
 * @author diesieben07
 */
public interface BiConsumerX<T, U> {

    void accept(T t, U u) throws Exception;

}
