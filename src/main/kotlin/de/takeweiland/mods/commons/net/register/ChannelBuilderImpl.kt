package de.takeweiland.mods.commons.net.register

import de.takeweiland.mods.commons.net.Packet
import io.netty.buffer.ByteBuf
import net.minecraftforge.fml.common.network.NetworkRegistry

/**
 * @author Take Weiland
 */
internal class ChannelBuilderImpl(val channel: String) : ChannelBuilderDslContext(), ChannelBuilder {

    data class RegisteredPacket<P : Packet>(val packet: Class<P>, val constructor: (ByteBuf) -> P)

    val packets = HashMap<Int, RegisteredPacket<*>>()

    override fun <P : Packet> add(id: Int, packet: Class<P>, constructor: (ByteBuf) -> P) {
        if (packets.putIfAbsent(id, RegisteredPacket(packet, constructor)) != null) {
            throw IllegalArgumentException("Duplicate packet ID $id for channel $channel")
        }
    }

    override fun register() {
        NetworkRegistry.
    }
}