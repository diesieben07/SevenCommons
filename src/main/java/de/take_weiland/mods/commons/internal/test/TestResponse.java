package de.take_weiland.mods.commons.internal.test;

import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.Packet;

/**
 * @author diesieben07
 */
class TestResponse implements Packet.Response {

    final String s;

    TestResponse(String s) {
        this.s = s;
    }

    TestResponse(MCDataInput in) {
        this.s = in.readString();
    }

    @Override
    public void writeTo(MCDataOutput out) {
        out.writeString(s);
    }
}
