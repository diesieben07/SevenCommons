package de.take_weiland.mods.commons.internal.net;

import java.io.IOException;

/**
 * @author diesieben07
 */
public interface PartTracker {

    void onPart(byte[] part);

    void start(String channel, int len) throws IOException;

    byte[] checkDone();

    String channel();

}
