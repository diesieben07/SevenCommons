package de.take_weiland.mods.commons.net;

/**
 * @author diesieben07
 */
final class BufferConstants {

    static final int ITEM_NULL_ID = 32000;
    static final int BLOCK_NULL_ID = 4096;
    static final int NO_MARK = -1;
    static final int SEVEN_BITS = 0b0111_1111;
    static final int BYTE_MSB = 0b1000_0000;
    static final int BOOLEAN_NULL = -1;
    static final int BOOLEAN_TRUE = 1;
    static final int BOOLEAN_FALSE = 0;
    static final int BOX_NULL = 0;
    static final int BOX_NONNULL = 1;
    static final long UUID_NULL_MSB = 0xF000;

    private BufferConstants() {
    }
}