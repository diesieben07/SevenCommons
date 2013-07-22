package de.take_weiland.mods.commons.updater;

public class InvalidModVersionException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidModVersionException(String message) {
		super(message);
	}

	public InvalidModVersionException(String message, Throwable cause) {
		super(message, cause);
	}

}
