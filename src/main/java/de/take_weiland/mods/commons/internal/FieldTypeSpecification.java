package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.serialize.SerializationMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * @author diesieben07
 */
final class FieldTypeSpecification<T> extends MemberTypeSpecification<Field, T> {

	FieldTypeSpecification(SerializationMethod serializationMethod, Field member) {
		super(serializationMethod, member);
	}

	@Override
	Class<?> getRawType0() {
		return member.getType();
	}

	@Override
	Type getGenericType0() {
		return member.getGenericType();
	}
}
