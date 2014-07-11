package de.take_weiland.mods.commons.meta;

/**
 * <p>An interface to be implemented by Subtypes of a Block or Item. Used in conjunction with {@link de.take_weiland.mods.commons.meta.HasSubtypes}</p>
 *
 * @author diesieben07
 */
public interface Subtype {

	/**
	 * <p>The name of this subtype. Will be used with the Block's or Item's unlocalized name to construct the full name.</p>
	 *
	 * @return a name for this subtype
	 */
	String subtypeName();

}
