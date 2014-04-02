package de.take_weiland.mods.commons.util;

import cpw.mods.fml.common.FMLLog;
import de.take_weiland.mods.commons.asm.ASMUtils;
import de.take_weiland.mods.commons.fastreflect.Fastreflect;

import java.util.logging.Logger;

public final class MiscUtil {

	private MiscUtil() { }
	
	private static SCReflector reflector;

	/**
	 * Obtain an instance of {@link de.take_weiland.mods.commons.util.SCReflector}
	 */
	public static SCReflector getReflector() {
		return reflector == null ? (reflector = Fastreflect.createAccessor(SCReflector.class)) : reflector;
	}

	public static Logger getLogger() {
		return getLogger(Fastreflect.getCallerClass().getSimpleName());
	}

	public static Logger getLogger(String channel) {
		FMLLog.makeLog(channel);
		return Logger.getLogger(channel);
	}

	/**
	 * determine if the code is running in a development environment
	 * @return true if this is a development environment
	 */
	public static boolean isDevelopmentEnv() {
		return ASMUtils.useMcpNames();
	}
}
