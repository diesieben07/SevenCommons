package de.take_weiland.mods.commons.asm;

import org.objectweb.asm.Type;

import java.lang.reflect.Method;

/**
 * @author diesieben07
 */
class MethodInfoReflect extends MethodInfo {

	private final Method method;

	MethodInfoReflect(ClassInfo clazz, Method method) {
		super(clazz);
		this.method = method;
	}

	@Override
	public String name() {
		return method.getName();
	}

	@Override
	public String desc() {
		return Type.getMethodDescriptor(method);
	}

	@Override
	public int modifiers() {
		return method.getModifiers();
	}
}
