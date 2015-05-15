package de.take_weiland.mods.commons.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * <p>Logging utilities.</p>
 */
public final class Logging {

    /**
     * <p>Create a new Logger suitable for use in Minecraft using the given channel.</p>
     *
     * @param channel the logging channel
     * @return a Logger
     */
    public static Logger getLogger(String channel) {
        return LogManager.getLogger(channel);
    }

    private Logging() {
    }

}
