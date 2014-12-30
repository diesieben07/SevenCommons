package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.util.JavaUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Method;

/**
 * @author diesieben07
 */
// todo make this faster
final class GetterSetterProperty extends AbstractProperty {

	private final Method getter;
	private final Method setter;
	private final Object target;

	GetterSetterProperty(Method getter, Method setter, Object target) {
		this.getter = getter;
		this.setter = setter;
		this.target = target;

		getter.setAccessible(true);
		setter.setAccessible(true);
	}

	@Override
	public Object get() {
		try {
			return getter.invoke(target, ArrayUtils.EMPTY_OBJECT_ARRAY);
		} catch (Exception e) {
			throw JavaUtils.throwUnchecked(e);
		}
	}

	@Override
	public void set(Object value) {
		try {
			setter.invoke(target, value);
		} catch (Exception e) {
			throw JavaUtils.throwUnchecked(e);
		}
	}
}
