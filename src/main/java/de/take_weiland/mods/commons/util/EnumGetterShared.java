package de.take_weiland.mods.commons.util;

import sun.misc.JavaLangAccess;
import sun.misc.SharedSecrets;
import de.take_weiland.mods.commons.util.JavaUtils.EnumValueGetter;

class EnumGetterShared implements EnumValueGetter {

	private JavaLangAccess langAcc = SharedSecrets.getJavaLangAccess();
	
	@Override
	public <T extends Enum<T>> T[] getEnumValues(Class<T> clazz) {
		System.out.println("getting enum constants with JavaLangAccess");
		return langAcc.getEnumConstantsShared(clazz);
	}

}
