package de.take_weiland.mods.commons.internal.sync;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Field;

/**
 * @author diesieben07
 */
public final class FieldProperty extends UnsafeDataProperty<Field> {

	private final long fieldOff;

	public FieldProperty(Field field, Field dataField) {
		super(field, dataField);
		fieldOff = unsafe.objectFieldOffset(field);
	}

	@Override
	public Object get(Object instance) {
		return unsafe.getObject(instance, fieldOff);
	}

	@Override
	public void set(Object value, Object instance) {
		unsafe.putObject(instance, fieldOff, value);
	}

	@Override
	TypeToken<?> resolveType() {
		return TypeToken.of(member.getGenericType());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? super Object> getRawType() {
		return (Class<? super Object>) member.getType();
	}
}
