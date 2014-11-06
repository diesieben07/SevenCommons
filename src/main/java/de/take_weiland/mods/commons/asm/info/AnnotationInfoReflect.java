package de.take_weiland.mods.commons.asm.info;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author diesieben07
 */
class AnnotationInfoReflect extends AnnotationInfo {

	private final Annotation annotation;

	AnnotationInfoReflect(HasAnnotations holder, Annotation annotation) {
		super(holder);
		this.annotation = annotation;
	}

	@Override
	public Type type() {
		return Type.getType(annotation.annotationType());
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

	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> getProperty(String prop) {
		try {
			return Optional.of((T) annotation.annotationType().getMethod(prop).invoke(annotation));
		} catch (NoSuchMethodException e) {
			return Optional.absent();
		} catch (ReflectiveOperationException e) {
			throw Throwables.propagate(e);
		}
	}
}
