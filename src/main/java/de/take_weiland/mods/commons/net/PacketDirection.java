package de.take_weiland.mods.commons.net;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * <p>Specify whether a {@link de.take_weiland.mods.commons.net.ModPacket} is send from the client to the server
 * or the other way around.</p>
 * <p>If a packet is received on the wrong side, the player is kicked with a {@link de.take_weiland.mods.commons.net.ProtocolException}.</p>
 * <p>The default value is {@link Dir#BOTH_WAYS}.</p>
 *
 * @author diesieben07
 */
@Retention(CLASS)
@Target(TYPE)
public @interface PacketDirection {

	Dir value();

	enum Dir {

		/**
		 * <p>The packet is only send from the client to the server.</p>
		 */
		TO_SERVER,
		/**
		 * <p>The packet is only send from the server to the client.</p>
		 */
		TO_CLIENT,
		/**
		 * <p>The packet can be send both ways. This is the default value.</p>
		 */
		BOTH_WAYS

	}

}
