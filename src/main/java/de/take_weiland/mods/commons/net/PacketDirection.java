package de.take_weiland.mods.commons.net;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * @author diesieben07
 */
@Retention(CLASS)
@Target(TYPE)
public @interface PacketDirection {

	Dir value();

	enum Dir {

		TO_SERVER,
		TO_CLIENT,
		BOTH_WAYS

	}

}
