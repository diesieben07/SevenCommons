package de.take_weiland.mods.commons.util;

import cpw.mods.fml.common.FMLLog;

import java.util.logging.Logger;

/**
 * <p>Logging utilities.</p>
 */
public final class Logging {

	/**
	 * <p>Create a new Logger suitable for use in Minecraft using the given channel.</p>
	 * @param channel the logging channel
	 * @return a Logger
	 */
	public static Logger getLogger(String channel) {
		FMLLog.makeLog(channel);
		return Logger.getLogger(channel);
	}

	private Logging() { }

}
