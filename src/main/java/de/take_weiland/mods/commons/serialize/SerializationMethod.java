package de.take_weiland.mods.commons.serialize;

/**
 * <p>Methods of serialization.</p>
 */
public enum SerializationMethod {

	/**
	 * <p>Pick the most appropriate for the type.</p>
	 */
	DEFAULT,

	/**
	 * <p>Serialize the type as a value.</p>
	 */
	VALUE,
	/**
	 * <p>Serialize the contents of the type.</p>
	 */
	CONTENTS
}
