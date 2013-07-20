package de.take_weiland.mods.commons.network;

public class NetworkException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NetworkException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public NetworkException(String message) {
		super(message);
	}
}
