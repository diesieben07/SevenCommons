package de.take_weiland.mods.commons.util;

import de.take_weiland.mods.commons.internal.CommonsModContainer;

public final class MiscUtil {

	private MiscUtil() { }
	
	public static SCReflector getReflector() {
		return CommonsModContainer.reflector;
	}
	
}
