package de.take_weiland.mods.commons.meta;

import com.google.common.collect.Maps;
import de.take_weiland.mods.commons.util.JavaUtils;

import java.util.Map;

/**
 * @author diesieben07
 */
class EnumProperty<T extends Enum<T>> extends AbstractArrayProperty<T> {

	EnumProperty(int shift, Class<T> clazz) {
		super(shift, JavaUtils.getEnumConstantsShared(clazz));
	}

	@Override
	int toMeta0(T value) {
		return value.ordinal();
	}

	@Override
	public <V> Map<T, V> createMap() {
		return Maps.newEnumMap(values[0].getDeclaringClass());
	}
}
