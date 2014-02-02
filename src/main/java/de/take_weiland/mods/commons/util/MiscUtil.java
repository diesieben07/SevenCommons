package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.fastreflect.Fastreflect;

public final class MiscUtil {

	private MiscUtil() { }
	
	private static SCReflector reflector;
	
	public static SCReflector getReflector() {
		return reflector == null ? (reflector = Fastreflect.createAccessor(SCReflector.class)) : reflector;
	}
	
}
