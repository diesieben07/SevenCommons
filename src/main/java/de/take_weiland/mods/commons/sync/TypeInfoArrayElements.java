package de.take_weiland.mods.commons.sync;

import com.google.common.reflect.TypeToken;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author diesieben07
 */
public final class TypeInfoArrayElements extends AbstractTypeInfo {

	private final SyncTypeInfo arrayType;

	public TypeInfoArrayElements(SyncTypeInfo arrayType) {
		this.arrayType = arrayType;
		checkArgument(arrayType.getRawType().isArray());
	}

	@Override
	protected TypeToken<?> resolveGenericType() {
		return arrayType.getGenericType().getComponentType();
	}

	@Override
	public Class<?> getRawType() {
		return arrayType.getRawType().getComponentType();
	}
}
