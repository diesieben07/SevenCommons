package de.take_weiland.mods.commons.asm.info;

import static org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * <p>Information about a Member of a class, such as a field or method.</p>
 * @author diesieben07
 */
public abstract class MemberInfo extends HasModifiers implements HasAnnotations {

	MemberInfo() { }

	/**
	 * <p>Get the name of this member.</p>
	 *
	 * @return the name
	 */
	public abstract String name();

	/**
	 * <p>Get the class containing this member.</p>
	 *
	 * @return the containing class
	 */
	public abstract ClassInfo containingClass();

	/**
	 * <p>Determine if this member is static.</p>
	 *
	 * @return true if this member is static
	 */
	public boolean isStatic() {
		return hasModifier(ACC_STATIC);
	}

}
