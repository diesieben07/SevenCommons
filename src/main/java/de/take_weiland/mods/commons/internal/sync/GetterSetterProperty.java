package de.take_weiland.mods.commons.internal.sync;

import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.util.JavaUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author diesieben07
 */
// todo make this faster
final class GetterSetterProperty extends UnsafeDataProperty<Method> {

	private final Method setter;

	GetterSetterProperty(Method getter, Method setter, Field dataField, Class<? extends Annotation> annotationClass) {
		super(getter, dataField, annotationClass);
		this.setter = setter;

		getter.setAccessible(true);
		if (setter != null) {
			setter.setAccessible(true);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? super Object> getRawType() {
		return (Class<? super Object>) member.getReturnType();
	}

	@Override
	TypeToken<?> resolveType() {
		return TypeToken.of(member.getGenericReturnType());
	}

	@Override
	public Object get(Object instance) {
		try {
			return member.invoke(instance, ArrayUtils.EMPTY_OBJECT_ARRAY);
		} catch (Exception e) {
			throw JavaUtils.throwUnchecked(e);
		}
	}

	@Override
	public void set(Object value, Object instance) {
		if (setter == null) {
			throw new UnsupportedOperationException("Write on non-writable property");
		}
		try {
			setter.invoke(instance, value);
		} catch (Exception e) {
			throw JavaUtils.throwUnchecked(e);
		}
	}
}
