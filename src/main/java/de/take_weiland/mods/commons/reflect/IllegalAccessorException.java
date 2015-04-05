package de.take_weiland.mods.commons.reflect;

/**
 * @author diesieben07
 */
public class IllegalAccessorException extends RuntimeException {

    public IllegalAccessorException(String message) {
        super(message);
    }

    public IllegalAccessorException(String message, Throwable cause) {
        super(message, cause);
    }
}
