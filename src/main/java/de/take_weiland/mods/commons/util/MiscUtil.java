package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.fastreflect.Fastreflect;

public final class MiscUtil {

	private MiscUtil() { }
	
	private static SCReflector reflector;

	/**
	 * Obtain an instance of {@link de.take_weiland.mods.commons.util.SCReflector}
	 */
	public static SCReflector getReflector() {
		return reflector == null ? (reflector = Fastreflect.createAccessor(SCReflector.class)) : reflector;
	}
	
}
