package de.take_weiland.mods.commons.asm;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author diesieben07
 */
public abstract class MethodInfo {

	private final ClassInfo clazz;

	MethodInfo(ClassInfo clazz) {
		this.clazz = clazz;
	}

	public abstract String name();

	public abstract String desc();

	public abstract int modifiers();

	public ClassInfo containingClass() {
		return clazz;
	}

	public boolean hasModifier(int mod) {
		return (modifiers() & mod) == mod;
	}

	public boolean isAbstract() {
		return hasModifier(ACC_ABSTRACT);
	}

	public boolean isStatic() {
		return hasModifier(ACC_STATIC);
	}

	public boolean isPrivate() {
		return hasModifier(ACC_PRIVATE);
	}

	public boolean isConstructor() {
		return name().equals("<init>");
	}

	public boolean isStaticInit() {
		return name().equals("<clinit>");
	}

}
