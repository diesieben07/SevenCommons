package de.take_weiland.mods.commons.net;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>An Exception to be thrown packet handling code to indicate a protocol violation.</p>
 * @author diesieben07
 */
public class ProtocolException extends RuntimeException {

	private String playerKickMsg;

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

	/**
	 * <p>Let this Exception kick the player, in case the packet is received on the server.</p>
	 * @return this, for convenience
	 */
	public final ProtocolException kickPlayer() {
		return kickPlayer(getMessage());
	}

	/**
	 * <p>Let this Exception kick the player with the specified message, in case the packet is received on the server.</p>
	 * @return this, for convenience
	 */
	public final ProtocolException kickPlayer(String msg) {
		playerKickMsg = checkNotNull(msg, "No kick message");
		return this;
	}

    public final String getKickMessage() {
        return playerKickMsg;
    }

}
