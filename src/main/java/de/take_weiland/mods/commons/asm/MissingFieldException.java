package de.take_weiland.mods.commons.asm;

/**
 * <p>Indicates that a required field could not be found.</p>
 * @author diesieben07
 */
public class MissingFieldException extends RuntimeException {

	MissingFieldException(String clazz, String field) {
		super(String.format("Missing required field %s in class %s", field, clazz));
	}

}
