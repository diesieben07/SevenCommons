package de.take_weiland.mods.commons.internal.sync;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author diesieben07
 */
final class MethodMetadata extends MemberMetadata<Method> {

	MethodMetadata(Method member) {
		super(member);
	}

	@Override
	Type getGenericType() {
		return member.getGenericReturnType();
	}

	@Override
	public Class<?> getRawType() {
		return member.getReturnType();
	}
}
