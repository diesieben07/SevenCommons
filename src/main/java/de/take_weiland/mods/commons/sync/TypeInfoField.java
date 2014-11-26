package de.take_weiland.mods.commons.sync;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Field;

/**
 * @author diesieben07
 */
public final class TypeInfoField extends AbstractTypeInfo {

	private final Field field;

	public TypeInfoField(Field field) {
		this.field = field;
	}

	@Override
	public Class<?> getRawType() {
		return field.getType();
	}

	@Override
	protected TypeToken<?> resolveGenericType() {
		return TypeToken.of(field.getGenericType());
	}
}
