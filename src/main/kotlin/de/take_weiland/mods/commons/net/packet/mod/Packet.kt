package de.take_weiland.mods.commons.net.packet.mod

import de.take_weiland.mods.commons.net.packet.*
import de.take_weiland.mods.commons.net.simple.SimplePacket
import de.take_weiland.mods.commons.util.Scheduler
import de.take_weiland.mods.commons.util.clientPlayer
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.NetworkManager
import net.minecraftforge.fml.relauncher.Side
import java.util.concurrent.CompletionStage

/**
 * An interface that helps with writing custom payload packets.
 */
abstract class Packet : ModPacketNoResponse(), SimplePacket {

    /**
     * Called on the main Minecraft thread for the receiving logical side when this packet is received.
     *
     * @param side the logical side receiving this packet
     * @param player the player receiving this packet
     */
    abstract fun receive(side: Side, player: EntityPlayer)

    private fun scheduleReceive(side: Side, player: EntityPlayer?) {
        Scheduler.forSide(side).run { receive(side, player ?: clientPlayer) }
    }

    final override fun receiveAsync(side: Side, player: EntityPlayer?) {
        scheduleReceive(side, player)
    }

    final override fun receiveAfterReconstruction(side: Side, player: EntityPlayer?) {
        scheduleReceive(side, player)
    }

    /**
     * A version of [Packet] that is handled on an unspecified thread.
     */
    abstract class Async : ModPacketNoResponse() {

        /**
         * Called on an unspecified thread when this packet is received.
         *
         * @param side the logical side receiving this packet
         * @param player the player receiving this packet, may be `null` on the client
         */
        abstract fun receive(side: Side, player: EntityPlayer?)

        final override fun receiveAsync(side: Side, player: EntityPlayer?) {
            receive(side, player)
        }

        final override fun receiveAfterReconstruction(side: Side, player: EntityPlayer?) {
            receive(side, player)
        }

    }

    /**
     * A version of [Packet] that has a response of type `R`.
     */
    abstract class WithResponse<out R : Response> : ModPacketWithResponse<R>(), SimplePacket.WithResponse<R> {

        /**
         * Called on the main Minecraft thread for the receiving logical side when this packet is received.
         *
         * @param side the logical side receiving this packet
         * @param player the player receiving this packet
         * @return the response to send back to sender of this packet
         */
        abstract fun receive(side: Side, player: EntityPlayer): R

        final override fun sendTo(manager: NetworkManager): CompletionStage<out R> {
            return WrappedPacketWithResponse(this, manager)
                    .also { manager.channel().writeAndFlush(it) }
        }

        final override fun receiveAfterReconstruction(responseId: Int, side: Side, player: EntityPlayer?) {
            Scheduler.forSide(side).run {
                WrappedResponse(receive(side, player ?: clientPlayer), responseId)
                        .sendReturn(side, player)
            }
        }

        /**
         * A version of [Packet.WithResponse] that is handled on an unspecified thread.
         */
        abstract class Async<out R : Response> : ModPacketWithResponse<R>(), SimplePacket.WithResponse<R> {

            /**
             * Called on an unspecified thread when this packet is received.
             *
             * @param side the logical side receiving this packet
             * @param player the player receiving this packet, may be `null` on the client
             * @return the response to send back to sender of this packet
             */
            abstract fun receive(side: Side, player: EntityPlayer?): R

            final override fun sendTo(manager: NetworkManager): CompletionStage<out R> {
                return WrappedPacketWithResponseAsync(this, manager)
                        .also { manager.channel().writeAndFlush(it) }
            }

            final override fun receiveAfterReconstruction(responseId: Int, side: Side, player: EntityPlayer?) {
                WrappedResponse(receive(side, player), responseId).sendReturn(side, player)
            }

        }
    }

    /**
     * A version of [Packet] that has a response of type `R`, which is provided asynchronously as a `CompletionStage<R>`.
     */
    abstract class WithAsyncResponse<out R : Response> : ModPacketWithResponse<R>(), SimplePacket.WithResponse<R> {

        /**
         * Called on the main Minecraft thread for the receiving logical side when this packet is received.
         *
         * @param side the logical side receiving this packet
         * @param player the player receiving this packet
         * @return the response to send back to sender of this packet
         */
        abstract fun receive(side: Side, player: EntityPlayer): CompletionStage<out R>

        final override fun sendTo(manager: NetworkManager): CompletionStage<out R> {
            return WrappedPacketWithAsyncResponse(this, manager)
                    .also { manager.channel().writeAndFlush(it) }
        }

        final override fun receiveAfterReconstruction(responseId: Int, side: Side, player: EntityPlayer?) {
            Scheduler.forSide(side).run {
                handleAsyncResponse(responseId, side, player, receive(side, player ?: clientPlayer))
            }
        }

        /**
         * A version of [Packet.WithAsyncResponse] that is handled on an unspecified thread.
         */
        abstract class Async<out R : Response> : ModPacketWithResponse<R>(), SimplePacket.WithResponse<R> {

            /**
             * Called on an unspecified thread when this packet is received.
             *
             * @param side the logical side receiving this packet
             * @param player the player receiving this packet, may be `null` on the client
             * @return the response to send back to sender of this packet
             */
            abstract fun receive(side: Side, player: EntityPlayer?): CompletionStage<out R>

            final override fun sendTo(manager: NetworkManager): CompletionStage<out R> {
                return WrappedPacketWithAsyncResponseAsync(this, manager)
                        .also { manager.channel().writeAndFlush(it) }
            }

            final override fun receiveAfterReconstruction(responseId: Int, side: Side, player: EntityPlayer?) {
                handleAsyncResponse(responseId, side, player, receive(side, player))
            }
        }

    }

    abstract class Response : BaseModPacket()

    // Internal Methods

}