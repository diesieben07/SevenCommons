package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.net.MCDataOutputStream;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author diesieben07
 */
public interface ByteStreamSerializable {

	void writeTo(MCDataOutputStream out);

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface Deserializer {

	}

}
