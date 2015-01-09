package de.take_weiland.mods.commons.serialize;

/**
 * <p>Methods of serialization.</p>
 */
public enum SerializationMethod {

	/**
	 * <p>Prefer {@code VALUE}, but use {@code CONTENTS} if {@code VALUE} is not available.</p>
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
