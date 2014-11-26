package de.take_weiland.mods.commons.sync;

import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * @author diesieben07
 */
public final class TypeInfoDirect extends AbstractTypeInfo {

	private final Type type;

	public TypeInfoDirect(Type type) {
		this.type = type;
	}

	@Override
	protected TypeToken<?> resolveGenericType() {
		return TypeToken.of(type);
	}

	@Override
	public Class<?> getRawType() {
		return type instanceof Class ? (Class<?>) type : getGenericType().getRawType();
	}
}
