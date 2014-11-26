package de.take_weiland.mods.commons.sync;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Method;

/**
 * @author diesieben07
 */
public final class TypeInfoMethod extends AbstractTypeInfo {

	private final Method method;

	public TypeInfoMethod(Method method) {
		this.method = method;
	}

	@Override
	public Class<?> getRawType() {
		return method.getReturnType();
	}

	@Override
	protected TypeToken<?> resolveGenericType() {
		return TypeToken.of(method.getGenericReturnType());
	}
}
