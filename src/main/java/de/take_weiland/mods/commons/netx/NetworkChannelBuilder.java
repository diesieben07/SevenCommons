package de.take_weiland.mods.commons.netx;

import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;

import java.util.function.Function;

/**
 * <p>A builder for a NetworkChannel.</p>
 * <p>This interface allows the following fluid-style initialization code:</p>
 * <p><pre><code>
 *     Network.newChannel("channel")
 *         .register(PacketA::new, PacketA::receive)
 *         .register(...)
 *         .build();
 * </code></pre></p>
 * @author diesieben07
 */
public interface NetworkChannelBuilder<P> {

    NetworkChannelBuilder<P> register(Side side, Function<? super ByteBuf, ? extends P> decoder);

    NetworkChannelBuilder<P> register(Side side, int id, Function<? super ByteBuf, ? extends P> decoder);

    void build();

}
