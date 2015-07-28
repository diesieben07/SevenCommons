package de.take_weiland.mods.commons;

import cpw.mods.fml.common.eventhandler.Event;

/**
 * <p>Fired when the server saves the world data.</p>
 *
 * @author diesieben07
 */
public final class SaveWorldsEvent extends Event {

    /**
     * <p>Whether the server will log a message for this save event.</p>
     */
    public final boolean logMessage;

    public SaveWorldsEvent(boolean logMessage) {
        this.logMessage = logMessage;
    }

}
