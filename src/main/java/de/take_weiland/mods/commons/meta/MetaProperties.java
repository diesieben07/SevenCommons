package de.take_weiland.mods.commons.meta;

import static com.google.common.base.Preconditions.checkArgument;

final class MetaProperties {

    static int checkBit(int bit) {
        checkArgument(bit >= 0 && bit <= 31, "Invalid start bit");
        return bit;
    }

    static int checkBitCount(int bits) {
        checkArgument(bits >= 1 && bits <= 32, "Invalid bit count");
        return bits;
    }

    private MetaProperties() {
    }

}
