package de.take_weiland.mods.commons.asm;

/**
* @author diesieben07
*/
public class MissingClassException extends RuntimeException {

	public MissingClassException(String message) {
		super(message);
	}

	public MissingClassException(String message, Throwable cause) {
		super(message, cause);
	}
}
