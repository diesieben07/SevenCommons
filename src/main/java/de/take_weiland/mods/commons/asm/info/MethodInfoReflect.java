package de.take_weiland.mods.commons.asm.info;

import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
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

	@Override
	public boolean isConstructor() {
		return false;
	}

	@Override
	public AnnotationInfo getAnnotation(Class<? extends Annotation> annotation) {
		Annotation ann = method.getAnnotation(annotation);
		return ann == null ? null : new AnnotationInfoReflect(this, ann);
	}

	@Override
	public boolean hasAnnotation(Class<? extends Annotation> annotation) {
		return method.isAnnotationPresent(annotation);
	}
}
