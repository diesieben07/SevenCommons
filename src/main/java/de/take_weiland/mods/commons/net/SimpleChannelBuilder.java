package de.take_weiland.mods.commons.net;

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

    /**
     * <p>Register a packet using the given constructor and handler.</p>
     *
     * @param id          the packet ID
     * @param constructor the constructor for the packet
     * @param handler     the handler for the packet
     * @return this, for convenience
     */
    <P extends Packet> SimpleChannelBuilder register(int id, PacketConstructor<P> constructor, PacketHandler<? super P> handler);

    /**
     * <p>Register a packet using the given constructor and handler.</p>
     *
     * @param id          the packet ID
     * @param constructor the constructor for the packet
     * @param handler     the handler for the packet
     * @return this, for convenience
     */
    default <P extends Packet> SimpleChannelBuilder register(int id, PacketConstructor<P> constructor, PacketHandler.IncludingSide<? super P> handler) {
        return register(id, constructor, (PacketHandler<? super P>) handler);
    }

    /**
     * <p>Register a Packet with a response using the given constructors and handler.</p>
     * @param id the packet ID
     * @param constructor the constructor for the packet
     * @param responseConstructor the constructor for the response
     * @param handler the handler for the packet
     * @return this, for convenience
     */
    <P extends Packet.WithResponse<R>, R extends Packet.Response> SimpleChannelBuilder register(int id, PacketConstructor<P> constructor, PacketConstructor<R> responseConstructor, PacketHandler.WithResponse<? super P, ? extends R> handler);

    /**
     * <p>Register a Packet with a response using the given constructors and handler.</p>
     *
     * @param id                  the packet ID
     * @param constructor         the constructor for the packet
     * @param responseConstructor the constructor for the response
     * @param handler             the handler for the packet
     * @return this, for convenience
     */
    default <P extends Packet.WithResponse<R>, R extends Packet.Response> SimpleChannelBuilder register(int id, PacketConstructor<P> constructor, PacketConstructor<R> responseConstructor, PacketHandler.WithResponse.IncludingSide<? super P, ? extends R> handler) {
        return register(id, constructor, responseConstructor, (PacketHandler.WithResponse<? super P, ? extends R>) handler);
    }

    /**
     * <p>Register a Packet with an asynchronous response using the given constructors and the handler.</p>
     *
     * @param id                  the packet ID
     * @param constructor         the constructor for the packet
     * @param responseConstructor the constructor for the response
     * @param handler             the handler for the packet
     * @return this, for convenience
     */
    <P extends Packet.WithResponse<R>, R extends Packet.Response> SimpleChannelBuilder registerWithAsyncResponse(int id, PacketConstructor<P> constructor, PacketConstructor<R> responseConstructor, PacketHandler.WithAsyncResponse<? super P, ? extends R> handler);

    /**
     * <p>Register a Packet with an asynchronous response using the given constructors and the handler.</p>
     *
     * @param id                  the packet ID
     * @param constructor         the constructor for the packet
     * @param responseConstructor the constructor for the response
     * @param handler             the handler for the packet
     * @return this, for convenience
     */
    default <P extends Packet.WithResponse<R>, R extends Packet.Response> SimpleChannelBuilder registerWithAsyncResponse(int id, PacketConstructor<P> constructor, PacketConstructor<R> responseConstructor, PacketHandler.WithAsyncResponse.IncludingSide<? super P, ? extends R> handler) {
        return registerWithAsyncResponse(id, constructor, responseConstructor, (PacketHandler.WithAsyncResponse<? super P, ? extends R>) handler);
    }

    /**
     * <p>Finish building.</p>
     */
    void build();

}
