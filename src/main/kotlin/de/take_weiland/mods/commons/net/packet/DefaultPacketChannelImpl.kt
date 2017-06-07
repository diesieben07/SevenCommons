package de.take_weiland.mods.commons.net.packet

import de.take_weiland.mods.commons.net.ProtocolException
import de.take_weiland.mods.commons.net.packet.raw.CustomPayloadPacket
import de.take_weiland.mods.commons.net.packet.raw.DefaultPacketChannelBuilder
import de.take_weiland.mods.commons.net.packet.raw.PacketChannel
import de.take_weiland.mods.commons.util.toImmutable
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import java.util.concurrent.ConcurrentHashMap

/**
 * @author diesieben07
 */
internal class DefaultPacketChannelImpl(override val channel: String, packets: Iterable<RegisteredPacket<*>>) : PacketChannel {

    private val packets = packets.toList().toTypedArray()
    private val packetsByClass = packets.associateBy { it.packetClass }.toImmutable()

    override fun receive(buf: ByteBuf, player: EntityPlayer?) {
        val id = buf.readPacketId()
        val packet = packets.getOrNull(id)
        packet?.run {
            val pkt = with (reader) { buf.read() }
            (pkt as? CustomPayloadPacket)?.receiveAsync(player) ?: throw ProtocolException("Received invalid packet $pkt")
        } ?: throw ProtocolException("Unknown packetId $id on channel $channel")
    }

}

internal class DefaultPacketChannelBuilderImpl(channelName: String) : DefaultPacketChannelBuilder(channelName) {

    private var nextId = 0
    private val packets = HashMap<Class<out BasePacketNoResponse>, RegisteredPacket<*>>()

    override fun <T : BasePacketNoResponse> add(packetClass: Class<T>, reader: PacketReader<T>) {
        require(!globalDataStorageMap.containsKey(packetClass) && packets.putIfAbsent(packetClass, RegisteredPacket(nextId, channelName, packetClass, reader)) == null) {
            "Packet class ${packetClass.name} cannot be reused."
        }
        nextId++
    }

    override fun build(): PacketChannel {
        globalDataStorageMap.putAll(packets)
        return DefaultPacketChannelImpl(channelName, packets.values).also {
            if (globalPacketChannelMap.putIfAbsent(channelName, it) != null) throw IllegalArgumentException("Channel $channelName was already registered.")
        }
    }
}

internal data class RegisteredPacket<T : BasePacket>(val packetId: Int, val channel: String, val packetClass: Class<T>, val reader: PacketReader<T>)

private val globalDataStorageMap = ConcurrentHashMap<Class<*>, RegisteredPacket<*>>()
internal val globalPacketChannelMap = ConcurrentHashMap<String, PacketChannel>()

internal object GlobalPacketDataStorage : ClassValue<RegisteredPacket<*>>() {

    override fun computeValue(type: Class<*>): RegisteredPacket<*> {
        return globalDataStorageMap[type] ?: throw IllegalStateException("Packet class ${type.name} was not registered.")
    }

}

@Suppress("UNCHECKED_CAST")
internal val <T : BasePacket> T.data: RegisteredPacket<T>
    inline get() = GlobalPacketDataStorage[javaClass] as RegisteredPacket<T>