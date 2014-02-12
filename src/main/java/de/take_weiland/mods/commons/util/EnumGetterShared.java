package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.util.JavaUtils.EnumValueGetter;
import sun.misc.JavaLangAccess;
import sun.misc.SharedSecrets;

class EnumGetterShared implements EnumValueGetter {

	private JavaLangAccess langAcc = SharedSecrets.getJavaLangAccess();
	
	@Override
	public <T extends Enum<T>> T[] getEnumValues(Class<T> clazz) {
		return langAcc.getEnumConstantsShared(clazz);
	}

}
