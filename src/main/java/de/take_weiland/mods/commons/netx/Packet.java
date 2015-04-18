package de.take_weiland.mods.commons.netx;

        import io.netty.buffer.ByteBuf;

/**
 * @author diesieben07
 */
public interface Packet {

    void write(ByteBuf buf);

    default int expectedSize() {
        return 32;
    }

    interface WithResponse<R extends Packet> extends Packet {

    }

}
