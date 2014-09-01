package de.take_weiland.mods.commons.asm.info;

import java.lang.annotation.Annotation;

import static org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * @author diesieben07
 */
public abstract class MemberInfo extends HasModifiers {
	/**
	 * <p>Get the name of this member.</p>
	 *
	 * @return the name
	 */
	public abstract String name();

	/**
	 * <p>Get the class containing this member.</p>
	 *
	 * @return the ClassInfo
	 */
	public abstract ClassInfo containingClass();

	public abstract AnnotationInfo getAnnotation(Class<? extends Annotation> annotation);

	public abstract boolean hasAnnotation(Class<? extends Annotation> annotation);

	/**
	 * <p>Determine if this member is static.</p>
	 *
	 * @return true if this member is static
	 */
	public boolean isStatic() {
		return hasModifier(ACC_STATIC);
	}

}
