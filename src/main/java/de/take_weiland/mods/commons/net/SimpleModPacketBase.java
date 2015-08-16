package de.take_weiland.mods.commons.net;

/**
 * <p>Base interface for all {@link Packet} and related interfaces.</p>
 * @author diesieben07
 */
interface SimpleModPacketBase {

    /**
     * <p>Encode this packet's data into the output stream.</p>
     *
     * @param out the output stream
     */
    void writeTo(MCDataOutput out);

    /**
     * <p>The expected amount of bytes that this packet will produce when encoded.</p>
     * <p>A slight overestimation is better than an underestimation.</p>
     *
     * @return the expected amount of bytes
     */
    default int expectedSize() {
        return Network.DEFAULT_EXPECTED_SIZE;
    }

}
