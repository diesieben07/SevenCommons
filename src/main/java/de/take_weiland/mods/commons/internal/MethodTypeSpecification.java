package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.serialize.SerializationMethod;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author diesieben07
 */
final class MethodTypeSpecification<T> extends MemberTypeSpecification<Method, T> {

	MethodTypeSpecification(SerializationMethod serializationMethod, Method member) {
		super(serializationMethod, member);
	}

	@Override
	Class<?> getRawType0() {
		return member.getReturnType();
	}

	@Override
	Type getGenericType0() {
		return member.getGenericReturnType();
	}
}
