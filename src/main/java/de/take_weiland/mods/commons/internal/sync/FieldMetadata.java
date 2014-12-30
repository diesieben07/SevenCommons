package de.take_weiland.mods.commons.internal.sync;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * @author diesieben07
 */
final class FieldMetadata extends MemberMetadata<Field> {

	FieldMetadata(Field member) {
		super(member);
	}

	@Override
	Type getGenericType() {
		return member.getGenericType();
	}

	@Override
	public Class<?> getRawType() {
		return member.getType();
	}
}
