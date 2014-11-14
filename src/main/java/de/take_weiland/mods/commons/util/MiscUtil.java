package de.take_weiland.mods.commons.util;

import cpw.mods.fml.common.FMLLog;

import java.util.logging.Logger;

public final class MiscUtil {

	private MiscUtil() {
	}

	public static Logger getLogger(String channel) {
		FMLLog.makeLog(channel);
		return Logger.getLogger(channel);
	}

}
