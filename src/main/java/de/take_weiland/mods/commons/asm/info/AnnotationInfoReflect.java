package de.take_weiland.mods.commons.asm.info;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author diesieben07
 */
class AnnotationInfoReflect extends AnnotationInfo {

	private final Annotation annotation;

	AnnotationInfoReflect(Annotation annotation) {
		this.annotation = annotation;
	}

	@Override
	public boolean hasProperty(String prop) {
		Class<?> clazz = annotation.annotationType();
		for (Method method : clazz.getMethods()) {
			if (method.getName().equals(prop)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public <T> T getProperty(String prop, T defaultValue) {
		Class<?> clazz = annotation.annotationType();
		for (Method method : clazz.getMethods()) {
			if (method.getName().equals(prop)) {
				try {
					@SuppressWarnings("unchecked")
					T r = (T) method.invoke(annotation);
					return r;
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return defaultValue;
	}
}
