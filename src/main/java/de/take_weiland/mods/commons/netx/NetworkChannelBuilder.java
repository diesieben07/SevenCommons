package de.take_weiland.mods.commons.netx;

/**
 * <p>A builder for a {@link NetworkChannel}.</p>
 * <p>This interface allows the following fluid-style initialization code:</p>
 * <p><pre><code>
 *     NetworkChannel channel = Network.newChannel("channel")
 *         .register(PacketA::new, PacketA::receive)
 * </code></pre></p>
 * @author diesieben07
 */
public interface NetworkChannelBuilder {

    <P extends Packet, H extends PacketHandler<P>> NetworkChannelBuilder register(PacketConstructor<P> packet, H handler);
    <P extends Packet, H extends PacketHandler<P>> NetworkChannelBuilder register(int id, PacketConstructor<P> packet, H handler);

    <P extends Packet.WithResponse<R>, R extends Packet, H extends PacketHandler.WithResponse<P, R>> NetworkChannelBuilder register(PacketConstructor.ForWithResponse<P, R> packet, H handler);
    <P extends Packet.WithResponse<R>, R extends Packet, H extends PacketHandler.WithResponse<P, R>> NetworkChannelBuilder register(int id, PacketConstructor.ForWithResponse<P, R> packet, H handler);

    NetworkChannel build();

}
