package de.take_weiland.mods.commons.net.packet

import de.take_weiland.mods.commons.net.packet.mod.ModPacketNoResponse
import de.take_weiland.mods.commons.net.packet.mod.ModPacketWithResponse
import de.take_weiland.mods.commons.net.packet.mod.Packet
import de.take_weiland.mods.commons.net.packet.raw.PacketChannel
import io.netty.buffer.ByteBuf

/**
 * A builder for a `PacketChannel` that handles [Packet] packets.
 */
abstract class SimplePacketChannelBuilder internal constructor(protected val channelName: String) {

    /**
     * Finishes building this channel and registers it.
     */
    abstract fun create(): PacketChannel

    abstract fun <T : ModPacketNoResponse> packet(packetClass: Class<T>, reader: (ByteBuf) -> T)

    abstract fun <T : ModPacketWithResponse<R>, R : Packet.Response> packet(packetClass: Class<T>, reader: (ByteBuf) -> T,
                                                                            responseClass: Class<R>, responseReader: (ByteBuf) -> R)

    inline fun <reified T : ModPacketNoResponse> packet(noinline reader: (ByteBuf) -> T) {
        packet(T::class.java, reader)
    }

    inline fun <reified T : ModPacketWithResponse<R>, reified R : Packet.Response> packet(noinline reader: (ByteBuf) -> T, noinline responseReader: (ByteBuf) -> R) {
        packet(T::class.java, reader, R::class.java, responseReader)
    }

}