package de.take_weiland.mods.commons.util;

import cpw.mods.fml.common.FMLLog;
import de.take_weiland.mods.commons.fastreflect.Fastreflect;

import java.util.logging.Logger;

public final class MiscUtil {

	private MiscUtil() { }
	
	private static SCReflector reflector;

	public static Logger getLogger() {
		return getLogger(Fastreflect.getCallerClass().getSimpleName());
	}

	public static Logger getLogger(String channel) {
		FMLLog.makeLog(channel);
		return Logger.getLogger(channel);
	}

}
