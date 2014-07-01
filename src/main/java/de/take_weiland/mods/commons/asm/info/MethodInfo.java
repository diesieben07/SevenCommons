package de.take_weiland.mods.commons.asm.info;

import static org.objectweb.asm.Opcodes.*;

/**
 * <p>Some information about a method.</p>
 * @author diesieben07
 */
public abstract class MethodInfo implements HasModifiers {

	private final ClassInfo clazz;

	MethodInfo(ClassInfo clazz) {
		this.clazz = clazz;
	}

	/**
	 * <p>Get the name of the method represented by this MethodInfo.</p>
	 * @return the method name
	 */
	public abstract String name();

	/**
	 * <p>Get the method descriptor of the method represented by this MethodInfo.</p>
	 * @return the method descriptor
	 */
	public abstract String desc();

	/**
	 * <p>Get a ClassInfo representing the class containing this method.</p>
	 * @return the containing class
	 */
	public ClassInfo containingClass() {
		return clazz;
	}

	@Override
	public boolean hasModifier(int mod) {
		return (modifiers() & mod) == mod;
	}

	/**
	 * <p>Determine if this method is abstract.</p>
	 * @return true if this method is abstract.
	 */
	public boolean isAbstract() {
		return hasModifier(ACC_ABSTRACT);
	}

	/**
	 * <p>Determine if this method is static.</p>
	 * @return true if this method is static
	 */
	public boolean isStatic() {
		return hasModifier(ACC_STATIC);
	}

	/**
	 * <p>Determine if this method is package-private (default visibility).</p>
	 * @return true if this method is package-private
	 */
	public boolean isPackagePrivate() {
		return !hasModifier(ACC_PRIVATE) && !hasModifier(ACC_PROTECTED) && !hasModifier(ACC_PUBLIC);
	}

	/**
	 * <p>Determine if this method is a constructor.</p>
	 * @return true if this method is a constructor
	 */
	public boolean isConstructor() {
		return name().equals("<init>");
	}

	/**
	 * <p>Determine if this method represents the static-initialization block ({@code &lt;clinit&gt;}).</p>
	 * @return true if this method represents the static-initialization block
	 */
	public boolean isStaticInit() {
		return name().equals("<clinit>");
	}

}
