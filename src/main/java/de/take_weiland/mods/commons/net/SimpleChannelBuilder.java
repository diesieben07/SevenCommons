package de.take_weiland.mods.commons.net;

import net.minecraft.entity.player.EntityPlayer;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * <p>A builder for a NetworkChannel.</p>
 * <p>This interface allows the following fluid-style initialization code:</p>
 * <p><pre><code>
 *     Network.newChannel("channel")
 *         .register(PacketA::new, PacketA::handle)
 *         .register(...)
 *         .build();
 *
 *     class PacketA implements Packet {
 *
 *         private final String msg;
 *
 *         PacketA(MCDataInput in) {
 *             msg = in.readString();
 *         }
 *
 *         PacketA(String msg) {
 *             this.msg = msg;
 *         }
 *
 *         &#64;Override
 *         void writeTo(MCDataOutput out) {
 *             out.writeString(msg);
 *         }
 *
 *         void handle(EntityPlayer player, Side side) {
 *             System.out.println(msg);
 *         }
 *     }
 * </code></pre></p>
 *
 * @author diesieben07
 */
public interface SimpleChannelBuilder {

    // these ridiculous overloads allow for all combinations of PacketConstructor versions (with or without player) and handler versions (SimplePacketHandler = with side / without side)
    // most useful for implementing handler and constructor via method references / lambdas, which is desired
    // they all delegate to the single abstract register method for either with or without response

    /**
     * <p>Register a packet using the given constructor and handler.</p>
     *
     * @param id          the packet ID
     * @param constructor the constructor for the packet
     * @param handler     the handler for the packet
     * @return this, for convenience
     */
    <P extends Packet> SimpleChannelBuilder register(int id, PacketConstructor<P> constructor, BiConsumer<? super P, ? super EntityPlayer> handler);

    default <P extends Packet> SimpleChannelBuilder register(int id, PacketConstructor<P> constructor, SimplePacketHandler<? super P> handler) {
        return register(id, constructor, (BiConsumer<? super P, EntityPlayer>) handler);
    }

    default <P extends Packet> SimpleChannelBuilder register(int id, PacketConstructor.WithoutPlayer<P> constructor, SimplePacketHandler<? super P> handler) {
        return register(id, (PacketConstructor<P>) constructor, (BiConsumer<? super P, EntityPlayer>) handler);
    }

    default <P extends Packet> SimpleChannelBuilder register(int id, PacketConstructor.WithoutPlayer<P> constructor, BiConsumer<? super P, ? super EntityPlayer> handler) {
        return register(id, (PacketConstructor<P>) constructor, handler);
    }

    /**
     * <p>Register a Packet with a response using the given constructor and handler.</p>
     * @param id the packet ID
     * @param constructor the constructor for the packet
     * @param responseConstructor the constructor for the response
     * @param handler the handler for the packet
     * @return this, for convenience
     */
    <P extends Packet.WithResponse<R>, R extends Packet.Response> SimpleChannelBuilder register(int id, PacketConstructor<P> constructor, PacketConstructor<R> responseConstructor, BiFunction<? super P, ? super EntityPlayer, ? extends R> handler);

    default <P extends Packet.WithResponse<R>, R extends Packet.Response> SimpleChannelBuilder register(int id, PacketConstructor<P> constructor, PacketConstructor<R> responseConstructor, SimplePacketHandler.WithResponse<? super P, ? extends R> handler) {
        return register(id, constructor, responseConstructor, (BiFunction<? super P, ? super EntityPlayer, ? extends R>) handler);
    }

    default <P extends Packet.WithResponse<R>, R extends Packet.Response> SimpleChannelBuilder register(int id, PacketConstructor.WithoutPlayer<P> constructor, PacketConstructor<R> responseConstructor, BiFunction<? super P, ? super EntityPlayer, ? extends R> handler) {
        return register(id, (PacketConstructor<P>) constructor, responseConstructor, handler);
    }

    default <P extends Packet.WithResponse<R>, R extends Packet.Response> SimpleChannelBuilder register(int id, PacketConstructor.WithoutPlayer<P> constructor, PacketConstructor<R> responseConstructor, SimplePacketHandler.WithResponse<? super P, ? extends R> handler) {
        return register(id, (PacketConstructor<P>) constructor, responseConstructor, (BiFunction<? super P, ? super EntityPlayer, ? extends R>) handler);
    }

    default <P extends Packet.WithResponse<R>, R extends Packet.Response> SimpleChannelBuilder register(int id, PacketConstructor<P> constructor, PacketConstructor.WithoutPlayer<R> responseConstructor, BiFunction<? super P, ? super EntityPlayer, ? extends R> handler) {
        return register(id, constructor, (PacketConstructor<R>) responseConstructor, handler);
    }

    default <P extends Packet.WithResponse<R>, R extends Packet.Response> SimpleChannelBuilder register(int id, PacketConstructor<P> constructor, PacketConstructor.WithoutPlayer<R> responseConstructor, SimplePacketHandler.WithResponse<? super P, ? extends R> handler) {
        return register(id, constructor, (PacketConstructor<R>) responseConstructor, (BiFunction<? super P, ? super EntityPlayer, ? extends R>) handler);
    }

    default <P extends Packet.WithResponse<R>, R extends Packet.Response> SimpleChannelBuilder register(int id, PacketConstructor.WithoutPlayer<P> constructor, PacketConstructor.WithoutPlayer<R> responseConstructor, BiFunction<? super P, ? super EntityPlayer, ? extends R> handler) {
        return register(id, (PacketConstructor<P>) constructor, (PacketConstructor<R>) responseConstructor, handler);
    }

    default <P extends Packet.WithResponse<R>, R extends Packet.Response> SimpleChannelBuilder register(int id, PacketConstructor.WithoutPlayer<P> constructor, PacketConstructor.WithoutPlayer<R> responseConstructor, SimplePacketHandler.WithResponse<? super P, ? extends R> handler) {
        return register(id, (PacketConstructor<P>) constructor, (PacketConstructor<R>) responseConstructor, (BiFunction<? super P, ? super EntityPlayer, ? extends R>) handler);
    }

    void build();

}
