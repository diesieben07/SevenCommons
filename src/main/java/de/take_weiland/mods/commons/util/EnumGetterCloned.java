package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.util.JavaUtils.EnumValueGetter;

class EnumGetterCloned implements EnumValueGetter {

	@Override
	public <T extends Enum<T>> T[] getEnumValues(Class<T> clazz) {
		return clazz.getEnumConstants();
	}

}
