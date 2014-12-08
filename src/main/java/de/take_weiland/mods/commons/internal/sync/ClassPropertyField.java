package de.take_weiland.mods.commons.internal.sync;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Field;

/**
 * @author diesieben07
 */
public final class ClassPropertyField<T> extends ClassPropertyMember<Field, T> {

	public ClassPropertyField(Field field) {
		super(field);
	}

	@Override
	protected TypeToken<?> resolveType() {
		return TypeToken.of(member.getGenericType());
	}
}
