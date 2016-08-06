package de.take_weiland.mods.commons.net;

import de.take_weiland.mods.commons.internal.net.PacketHandlerBaseWithResponse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.CompletionStage;

import static de.take_weiland.mods.commons.util.Sides.sideOf;

/**
 * <p>Handler for {@link Packet Packets}.</p>
 * <p>Registered using {@link Network#newSimpleChannel(String)}.</p>
 *
 * @author diesieben07
 */
@FunctionalInterface
public interface PacketHandler<P extends Packet> extends PacketHandlerBase {

    /**
     * <p>The {@code Async} annotation may be used on a class that implements any of the packet handler interfaces or a method that is used
     * as a method reference to implement it.</p>
     * <p>When marked with this annotation a packet handler will execute on the netty thread instead of the the main game thread.</p>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD })
    @interface Async {
    }

    /**
     * <p>The {@code ReceivingSide} annotation may be used on a class that implements any of the packet handler interfaces or a method that is used
     * as a method reference to implement it.</p>
     * <p>The value specified sets a valid side for this handler. If a packet for this handler arrives on the opposite side a {@link ProtocolException}
     * will be thrown. If this annotation is not present, the handler can receive packets on both sides.</p>
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD })
    @interface ReceivingSide {

        Side value();

    }

    /**
     * <p>Called when a packet is received.</p>
     * <p>On the client the client player will be passed, on the server the player sending the packet will be passed.</p>
     *
     * @param packet the packet
     * @param player the player
     */
    void handle(P packet, EntityPlayer player);

    /**
     * <p>Version of {@code PacketHandler} that provides a {@link Side} argument specifying the logical side.</p>
     */
    @FunctionalInterface
    interface WithSideAndPlayer<P extends Packet> extends PacketHandler<P> {

        /**
         * <p>Called when a packet is received.</p>
         * <p>On the client the client player will be passed, on the server the player sending the packet will be passed.</p>
         *
         * @param packet the packet
         * @param player the player
         * @param side   the logical side
         */
        void handle(P packet, EntityPlayer player, Side side);

        @Override
        default void handle(P packet, EntityPlayer player) {
            handle(packet, player, sideOf(player));
        }

    }

    /**
     * <p>Version of {@code PacketHandler} that provides a {@link Side} argument specifying the logical side, but no player argument.</p>
     */
    @FunctionalInterface
    interface WithSide<P extends Packet> extends PacketHandler<P> {

        /**
         * <p>Called when a packet is received.</p>
         * <p>On the client the client player will be passed, on the server the player sending the packet will be passed.</p>
         *
         * @param packet the packet
         * @param side   the logical side
         */
        void handle(P packet, Side side);

        @Override
        default void handle(P packet, EntityPlayer player) {
            handle(packet, sideOf(player));
        }

    }

    /**
     * <p>Version of {@code PacketHandler} that does not provide any arguments besides the packet.</p>
     */
    @FunctionalInterface
    interface WithoutPlayer<P extends Packet> extends PacketHandler<P> {

        /**
         * <p>Called when a packet is received.</p>
         * <p>On the client the client player will be passed, on the server the player sending the packet will be passed.</p>
         *
         * @param packet the packet
         */
        void handle(P packet);

        @Override
        default void handle(P packet, EntityPlayer player) {
            handle(packet);
        }

    }

    /**
     * <p>Version of {@code PacketHandler} for {@linkplain Packet.WithResponse packets with response}.</p>
     */
    @FunctionalInterface
    interface WithResponse<P extends Packet.WithResponse<R>, R extends Packet.Response> extends PacketHandlerBaseWithResponse.Plain<P, R> {

        /**
         * <p>Called when a packet is received.</p>
         * <p>On the client, the client player will be passed, on the server the player sending the packet will be passed.</p>
         *
         * @param packet the packet
         * @param player the player
         * @return the response
         */
        @Override
        R handle(P packet, EntityPlayer player);



        /**
         * <p>Version of {@code PacketHandler.WithResponse} that provides a {@link Side} argument specifying the logical side.</p>
         */
        @FunctionalInterface
        interface WithSideAndPlayer<P extends Packet.WithResponse<R>, R extends Packet.Response> extends PacketHandler.WithResponse<P, R> {

            /**
             * <p>Called when a packet is received.</p>
             * <p>On the client, the client player will be passed, on the server the player sending the packet will be passed.</p>
             *
             * @param packet the packet
             * @param player the player
             * @param side   the logical side
             * @return the response
             */
            R handle(P packet, EntityPlayer player, Side side);

            @Override
            default R handle(P packet, EntityPlayer player) {
                return handle(packet, player, sideOf(player));
            }
        }

        /**
         * <p>Version of {@code PacketHandler.WithResponse} that provides a {@link Side} argument specifying the logical side, but no player argument.</p>
         */
        @FunctionalInterface
        interface WithSide<P extends Packet.WithResponse<R>, R extends Packet.Response> extends PacketHandler.WithResponse<P, R> {

            /**
             * <p>Called when a packet is received.</p>
             * <p>On the client, the client player will be passed, on the server the player sending the packet will be passed.</p>
             *
             * @param packet the packet
             * @param side   the logical side
             * @return the response
             */
            R handle(P packet, Side side);

            @Override
            default R handle(P packet, EntityPlayer player) {
                return handle(packet, sideOf(player));
            }
        }

        /**
         * <p>Version of {@code PacketHandler.WithResponse} that does not provide any arguments besides the packet.</p>
         */
        @FunctionalInterface
        interface WithoutPlayer<P extends Packet.WithResponse<R>, R extends Packet.Response> extends PacketHandler.WithResponse<P, R> {

            /**
             * <p>Called when a packet is received.</p>
             * <p>On the client, the client player will be passed, on the server the player sending the packet will be passed.</p>
             *
             * @param packet the packet
             * @return the response
             */
            R handle(P packet);

            @Override
            default R handle(P packet, EntityPlayer player) {
                return handle(packet);
            }
        }

    }

    /**
     * <p>Version of {@code PacketHandler.WithResponse} that provides responses asynchronously.</p></p>
     */
    @FunctionalInterface
    interface WithAsyncResponse<P extends Packet.WithResponse<R>, R extends Packet.Response> extends PacketHandlerBaseWithResponse.Future<P, R> {

        /**
         * <p>Called when a packet is received.</p>
         * <p>On the client, the client player will be passed, on the server the player sending the packet will be passed.</p>
         *
         * @param packet the packet
         * @param player the player
         * @return the response as a {@code CompletionStage}
         */
        @Override
        CompletionStage<? extends R> handle(P packet, EntityPlayer player);

        /**
         * <p>Version of {@code PacketHandler.WithAsyncResponse} that provides a {@link Side} argument specifying the logical side.</p>
         */
        @FunctionalInterface
        interface WithSideAndPlayer<P extends Packet.WithResponse<R>, R extends Packet.Response> extends PacketHandler.WithAsyncResponse<P, R> {

            /**
             * <p>Called when a packet is received.</p>
             * <p>On the client, the client player will be passed, on the server the player sending the packet will be passed.</p>
             *
             * @param packet the packet
             * @param player the player
             * @param side   the logical side
             * @return the response as a {@code CompletionStage}
             */
            CompletionStage<? extends R> handle(P packet, EntityPlayer player, Side side);

            @Override
            default CompletionStage<? extends R> handle(P packet, EntityPlayer player) {
                return handle(packet, player, sideOf(player));
            }
        }

        /**
         * <p>Version of {@code PacketHandler.WithAsyncResponse} that provides a {@link Side} argument specifying the logical side, but no player argument.</p>
         */
        @FunctionalInterface
        interface WithSide<P extends Packet.WithResponse<R>, R extends Packet.Response> extends PacketHandler.WithAsyncResponse<P, R> {

            /**
             * <p>Called when a packet is received.</p>
             * <p>On the client, the client player will be passed, on the server the player sending the packet will be passed.</p>
             *
             * @param packet the packet
             * @param side   the logical side
             * @return the response as a {@code CompletionStage}
             */
            CompletionStage<? extends R> handle(P packet, Side side);

            @Override
            default CompletionStage<? extends R> handle(P packet, EntityPlayer player) {
                return handle(packet, sideOf(player));
            }
        }

        /**
         * <p>Version of {@code PacketHandler.WithAsyncResponse} that does not provide any arguments besides the packet.</p>
         */
        @FunctionalInterface
        interface WithoutPlayer<P extends Packet.WithResponse<R>, R extends Packet.Response> extends PacketHandler.WithAsyncResponse<P, R> {

            /**
             * <p>Called when a packet is received.</p>
             * <p>On the client, the client player will be passed, on the server the player sending the packet will be passed.</p>
             *
             * @param packet the packet
             * @return the response as a {@code CompletionStage}
             */
            CompletionStage<? extends R> handle(P packet);

            @Override
            default CompletionStage<? extends R> handle(P packet, EntityPlayer player) {
                return handle(packet);
            }
        }

    }
}
