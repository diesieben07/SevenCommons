package de.take_weiland.mods.commons.reflect;

/**
 * <p>Thrown by {@link SCReflection#createAccessor(Class)} to indicate that the accessor was invalid.</p>
 *
 * @author diesieben07
 */
public final class IllegalAccessorException extends RuntimeException {

    public IllegalAccessorException(String message) {
        super(message);
    }

    public IllegalAccessorException(String message, Throwable cause) {
        super(message, cause);
    }
}
