package de.take_weiland.mods.commons;

/**
 * @author diesieben07
 */
public class CannotSerializeException extends RuntimeException {

	public CannotSerializeException(Class<?> toSerialize) {
		super("No serialization method for " + toSerialize.getName());
	}
}
