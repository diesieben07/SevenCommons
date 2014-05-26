package de.take_weiland.mods.commons.asm;

/**
 * @author diesieben07
 */
public class NoMatchException extends RuntimeException {

	private static final long serialVersionUID = 2506604241429349354L;

	public NoMatchException() {
	}

	public NoMatchException(String message) {
		super(message);
	}
}
