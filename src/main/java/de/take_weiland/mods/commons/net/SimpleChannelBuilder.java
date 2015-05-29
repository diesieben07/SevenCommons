package de.take_weiland.mods.commons.net;

import net.minecraft.entity.player.EntityPlayer;

import java.util.function.BiConsumer;

/**
 * <p>A builder for a NetworkChannel.</p>
 * <p>This interface allows the following fluid-style initialization code:</p>
 * <p><pre><code>
 *     Network.newChannel("channel")
 *         .register(PacketA::new, PacketA::receive)
 *         .register(...)
 *         .build();
 * </code></pre></p>
 *
 * @author diesieben07
 */
public interface SimpleChannelBuilder {

    default <P extends Packet> SimpleChannelBuilder register(int id, PacketConstructor<? extends P> constructor, SimplePacketHandler<? super P> handler) {
        return register(id, constructor, (BiConsumer<? super P, EntityPlayer>) handler);
    }

    <P extends Packet> SimpleChannelBuilder register(int id, PacketConstructor<? extends P> constructor, BiConsumer<? super P, ? super EntityPlayer> handler);

    void build();

}
