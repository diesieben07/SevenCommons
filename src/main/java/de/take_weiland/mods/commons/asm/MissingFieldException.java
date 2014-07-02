package de.take_weiland.mods.commons.asm;

/**
 * @author diesieben07
 */
public class MissingFieldException extends RuntimeException {

	public MissingFieldException(String clazz, String field) {
		super(String.format("Missing required field %s in class %s", field, clazz));
	}

}
