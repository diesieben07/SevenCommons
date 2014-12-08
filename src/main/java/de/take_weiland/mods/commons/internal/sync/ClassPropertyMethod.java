package de.take_weiland.mods.commons.internal.sync;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Method;

/**
 * @author diesieben07
 */
public class ClassPropertyMethod<T> extends ClassPropertyMember<Method, T> {

	public ClassPropertyMethod(Method member) {
		super(member);
	}

	@Override
	protected TypeToken<?> resolveType() {
		return TypeToken.of(member.getGenericReturnType());
	}
}
