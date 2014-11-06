package de.take_weiland.mods.commons.asm.info;

import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author diesieben07
 */
class FieldInfoReflect extends FieldInfo {

	private final Field field;

	FieldInfoReflect(ClassInfo clazz, Field field) {
		super(clazz);
		this.field = field;
	}

	@Override
	public String desc() {
		return Type.getDescriptor(field.getType());
	}

	@Override
	public String name() {
		return field.getName();
	}

	@Override
	public int modifiers() {
		return field.getModifiers();
	}

	@Override
	public AnnotationInfo getAnnotation(Class<? extends Annotation> annotation) {
		Annotation ann = field.getAnnotation(annotation);
		return ann == null ? null : new AnnotationInfoReflect(this, ann);
	}

	@Override
	public boolean hasAnnotation(Class<? extends Annotation> annotation) {
		return field.isAnnotationPresent(annotation);
	}
}
