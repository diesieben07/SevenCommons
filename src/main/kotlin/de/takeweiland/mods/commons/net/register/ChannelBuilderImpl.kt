package de.takeweiland.mods.commons.net.register

import de.takeweiland.mods.commons.net.*
import de.takeweiland.mods.commons.net.registry.SimplePacketData
import de.takeweiland.mods.commons.net.registry.registerPlainPacket
import de.takeweiland.mods.commons.net.registry.registerResponsePacket
import de.takeweiland.mods.commons.netbase.globalPayloadHandlerRegistry
import io.netty.buffer.ByteBuf

/**
 * @author Take Weiland
 */
internal class ChannelBuilderImpl(val channel: String) : ChannelBuilderDslContext(), ChannelBuilder {

    sealed class Registration {
        abstract fun register(channel: String, id: Int): SimplePacketData<*>

        data class RegisteredPacket<P : Packet>(val packetClass: Class<P>, val factory: (ByteBuf) -> P) : Registration() {
            override fun register(channel: String, id: Int): SimplePacketData<*> = registerPlainPacket(channel, id, packetClass, factory)
        }
        data class RegisteredPacketWithResponse<P : AnyPacketWithResponse<R>, R : ResponsePacket>(
            val packetClass: Class<P>, val responseClass: Class<R>, val factory: (ByteBuf) -> P, val responseFactory: (ByteBuf) -> R
        ) : Registration() {
            override fun register(channel: String, id: Int): SimplePacketData<*> = registerResponsePacket(channel, id, packetClass, responseClass, factory, responseFactory)
        }
    }

    private val packets = HashMap<Int, Registration>()

    private fun validateId(id: Int) {
        if (id > MAX_PACKET_ID) {
            throw IllegalArgumentException("Invalid packet ID $id for channel $channel, must be <= $MAX_PACKET_ID")
        }
    }

    private fun doRegister(id: Int, reg: Registration) {
        validateId(id)
        if (packets.putIfAbsent(id, reg) != null) {
            throw IllegalArgumentException("Duplicate packet ID $id for channel $channel")
        }
    }

    override fun <P : Packet> add(id: Int, packet: Class<P>, constructor: (ByteBuf) -> P) {
        doRegister(id, Registration.RegisteredPacket(packet, constructor))
    }

    override fun <P : AnyPacketWithResponse<R>, R : ResponsePacket> add(
        id: Int, packet: Class<P>, responsePacket: Class<R>, constructor: (ByteBuf) -> P, responseConstructor: (ByteBuf) -> R
    ) {
        doRegister(id, Registration.RegisteredPacketWithResponse(packet, responsePacket, constructor, responseConstructor))
    }

    override fun register() {
        val maxId = packets.keys.max() ?: throw IllegalArgumentException("Cannot register packet channel $channel without packets.")
        val dataArray = arrayOfNulls<SimplePacketData<*>>(maxId + 1)
        for ((id, packet) in packets) {
            dataArray[id] = packet.register(channel, id)
        }

        globalPayloadHandlerRegistry.register(channel, PacketBasePayloadHandler(channel, dataArray))
    }
}