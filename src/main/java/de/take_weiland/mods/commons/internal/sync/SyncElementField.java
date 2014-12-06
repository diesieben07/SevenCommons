package de.take_weiland.mods.commons.internal.sync;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Field;

/**
 * @author diesieben07
 */
public final class SyncElementField<T> extends SyncElementMember<Field, T> {

	public SyncElementField(Field field) {
		super(field);
	}

	@Override
	protected TypeToken<?> resolveType() {
		return TypeToken.of(member.getGenericType());
	}
}
