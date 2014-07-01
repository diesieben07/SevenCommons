package de.take_weiland.mods.commons.asm.info;

import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;

/**
 * @author diesieben07
 */
class MethodInfoReflectCstr extends MethodInfo {

	private final Constructor<?> constructor;

	MethodInfoReflectCstr(ClassInfo clazz, Constructor<?> constructor) {
		super(clazz);
		this.constructor = constructor;
	}

	@Override
	public String name() {
		return "<init>";
	}

	@Override
	public String desc() {
		return Type.getConstructorDescriptor(constructor);
	}

	@Override
	public int modifiers() {
		return constructor.getModifiers();
	}

}
