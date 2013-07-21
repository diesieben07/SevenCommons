package de.take_weiland.mods.commons.network;

/**
 * Exception that gets thrown if something goes wrong during packet handling for your mod
 * @author diesieben07
 *
 */
public class NetworkException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NetworkException(String message, Throwable cause) {
		super(message, cause);
	}

	public NetworkException(String message) {
		super(message);
	}
}
