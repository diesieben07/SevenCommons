package de.take_weiland.mods.commons.meta;

import de.take_weiland.mods.commons.util.JavaUtils;

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
}
