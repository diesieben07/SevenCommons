package de.take_weiland.mods.commons.sync;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * @author diesieben07
 */
public final class TypeInfoGenericParameter extends AbstractTypeInfo {

	private final SyncTypeInfo baseType;
	private final Type parameter;

	public TypeInfoGenericParameter(SyncTypeInfo baseType, Type parameter) {
		this.baseType = baseType;
		this.parameter = parameter;
	}

	@Override
	protected TypeToken<?> resolveGenericType() {
		return baseType.getGenericType().resolveType(parameter);
	}

	@Override
	public Class<?> getRawType() {
		return getGenericType().getRawType();
	}

}
