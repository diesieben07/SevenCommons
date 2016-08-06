package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.internal.net.NetworkImpl;

import java.io.Serializable;

/**
 * <p>A PacketConstructor is used to instantiate a Packet object when it is received.</p>
 * <p>Usually a PacketConstructor is created via a <a href="https://docs.oracle.com/javase/tutorial/java/javaOO/methodreferences.html">constructor reference</a>
 * like so: {@code MyPacket::new}, where MyPacket is a class that implements Packet and has a constructor that takes a ByteBuf.</p>
 * <p>Under certain situations the actual generic type-parameter might not be found via reflection, in that case {@link #getPacketClass()}
 * must be overridden and implemented manually.</p>
 *
 * @author diesieben07
 */
@FunctionalInterface
public interface PacketConstructor<P extends PacketBase> extends Serializable {

    P newInstance(MCDataInput mcDataInput) throws Exception;

    default Class<P> getPacketClass() {
        return NetworkImpl.findPacketClassReflectively(this);
    }

}
