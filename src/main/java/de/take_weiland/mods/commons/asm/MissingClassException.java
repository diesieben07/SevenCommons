package de.take_weiland.mods.commons.asm;

/**
 * <p>Indicates that a required class could not be found.</p>
 *
 * @author diesieben07
 */
public class MissingClassException extends RuntimeException {

	MissingClassException(String message) {
		super(message);
	}

	MissingClassException(String message, Throwable cause) {
		super(message, cause);
	}
}
