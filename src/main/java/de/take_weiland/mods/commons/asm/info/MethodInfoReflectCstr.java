package de.take_weiland.mods.commons.asm.info;

import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
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

	@Override
	public boolean isConstructor() {
		return true;
	}

	@Override
	public AnnotationInfo getAnnotation(Class<? extends Annotation> annotation) {
		Annotation ann = constructor.getAnnotation(annotation);
		return ann == null ? null : new AnnotationInfoReflect(this, ann);
	}

	@Override
	public boolean hasAnnotation(Class<? extends Annotation> annotation) {
		return constructor.isAnnotationPresent(annotation);
	}
}
