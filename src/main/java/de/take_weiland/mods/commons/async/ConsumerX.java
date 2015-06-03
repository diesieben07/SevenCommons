package de.take_weiland.mods.commons.async;

/**
 * @author diesieben07
 */
public interface ConsumerX<T> {

    void accept(T t) throws Exception;

}
