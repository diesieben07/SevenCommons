package de.take_weiland.mods.commons.internal.test;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.Packet;

/**
 * @author diesieben07
 */
class TestPacket implements Packet.WithResponse<TestResponse> {

    final String s;

    TestPacket(String s) {
        this.s = s;
    }

    TestPacket(MCDataInput in) {
        this.s = in.readString();
    }

    @Override
    public void writeTo(MCDataOutput out) {
        out.writeString(s);
    }

    @Override
    public int expectedSize() {
        return s.length() << 1 + 5;
    }
}
