package de.take_weiland.mods.commons.asm;

/**
* @author diesieben07
*/
public class MissingClassException extends RuntimeException {

	private static final long serialVersionUID = 6600743760854228837L;

	public MissingClassException(String message) {
		super(message);
	}

	public MissingClassException(String message, Throwable cause) {
		super(message, cause);
	}
}
