package de.take_weiland.mods.commons.net;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author diesieben07
 */
public class ProtocolException extends Exception {

	public ProtocolException() {
		super();
	}

	public ProtocolException(String message) {
		super(message);
	}

	public ProtocolException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProtocolException(Throwable cause) {
		super(cause);
	}

	String playerKickMsg;

	public final ProtocolException kickPlayer() {
		return kickPlayer(getMessage());
	}

	public final ProtocolException kickPlayer(String msg) {
		playerKickMsg = checkNotNull(msg, "No kick message");
		return this;
	}

}
