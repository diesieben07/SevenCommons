package de.take_weiland.mods.commons.asm.info;

/**
 * @author diesieben07
 */
public interface HasModifiers {

	/**
	 * <p>Get all Java modifiers present on this element.</p>
	 * @return the modifiers
	 * @see java.lang.reflect.Modifier
	 */
	int modifiers();

	/**
	 * <p>Determine if the given Java language modifier is set on this element.</p>
	 * @param modifier the modifier to check
	 * @return true if the given modifier is set
	 * @see java.lang.reflect.Modifier
	 */
	boolean hasModifier(int modifier);

}
