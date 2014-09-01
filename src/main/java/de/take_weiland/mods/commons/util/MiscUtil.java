package de.take_weiland.mods.commons.util;

import cpw.mods.fml.common.FMLLog;
import de.take_weiland.mods.commons.reflect.SCReflection;

import java.util.logging.Logger;

public final class MiscUtil {

	private MiscUtil() {
	}

	public static Logger getLogger() {
		return getLogger(SCReflection.getCallerClass().getSimpleName());
	}

	public static Logger getLogger(String channel) {
		FMLLog.makeLog(channel);
		return Logger.getLogger(channel);
	}

}
