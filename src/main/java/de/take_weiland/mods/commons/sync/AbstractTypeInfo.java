package de.take_weiland.mods.commons.sync;

import com.google.common.reflect.TypeToken;

/**
 * @author diesieben07
 */
public abstract class AbstractTypeInfo implements SyncTypeInfo {

	protected abstract TypeToken<?> resolveGenericType();

	private TypeToken<?> genericType;

	@Override
	public final TypeToken<?> getGenericType() {
		return genericType == null ? (genericType = resolveGenericType()) : genericType;
	}
}
