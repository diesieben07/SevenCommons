package de.take_weiland.mods.commons.net;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;

import java.util.concurrent.CompletionStage;

import static de.take_weiland.mods.commons.util.Sides.sideOf;

/**
 * <p>Handler for {@link Packet Packets}.</p>
 * <p>Registered using {@link Network#newSimpleChannel(String)}.</p>
 *
 * @author diesieben07
 */
@FunctionalInterface
public interface PacketHandler<P extends Packet> {

    /**
     * <p>Called when a packet is received.</p>
     * <p>On the client the client player will be passed, on the server the player sending the packet will be passed.</p>
     *
     * @param packet the packet
     * @param player the player
     */
    void handle(P packet, EntityPlayer player);

    /**
     * <p>Version of {@code PacketHandler} that includes a {@link Side} argument specifying the logical side.</p>
     */
    @FunctionalInterface
    interface IncludingSide<P extends Packet> extends PacketHandler<P> {

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
     * <p>Version of {@code PacketHandler} for {@linkplain Packet.WithResponse packets with response}.</p>
     */
    @FunctionalInterface
    interface WithResponse<P extends Packet.WithResponse<R>, R extends Packet.Response> {

        /**
         * <p>Called when a packet is received.</p>
         * <p>On the client, the client player will be passed, on the server the player sending the packet will be passed.</p>
         *
         * @param packet the packet
         * @param player the player
         * @return the response
         */
        R handle(P packet, EntityPlayer player);

        /**
         * <p>Version of {@code PacketHandler.WithResponse} that includes a {@link Side} argument specifying the logical side.</p>
         */
        @FunctionalInterface
        interface IncludingSide<P extends Packet.WithResponse<R>, R extends Packet.Response> extends PacketHandler.WithResponse<P, R> {

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

    }

    /**
     * <p>Version of {@code PacketHandler.WithResponse} that provides responses asynchronously.</p></p>
     */
    @FunctionalInterface
    interface WithAsyncResponse<P extends Packet.WithResponse<R>, R extends Packet.Response> {

        /**
         * <p>Called when a packet is received.</p>
         * <p>On the client, the client player will be passed, on the server the player sending the packet will be passed.</p>
         *
         * @param packet the packet
         * @param player the player
         * @return the response as a {@code CompletionStage}
         */
        CompletionStage<? extends R> handle(P packet, EntityPlayer player);


        /**
         * <p>Version of {@code PacketHandler.WithAsyncResponse} that includes a {@link Side} argument specifying the logical side.</p>
         */
        @FunctionalInterface
        interface IncludingSide<P extends Packet.WithResponse<R>, R extends Packet.Response> extends PacketHandler.WithAsyncResponse<P, R> {

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

    }
}
