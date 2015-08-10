package de.take_weiland.mods.commons.net;

/**
 * @author diesieben07
 */
interface SimpleModPacketBase {

    default int expectedSize() {
        return Network.DEFAULT_EXPECTED_SIZE;
    }

}
