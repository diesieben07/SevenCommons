package de.take_weiland.mods.commons.net.mod

import de.take_weiland.mods.commons.net.ProtocolException
import de.take_weiland.mods.commons.net.readVarInt
import de.take_weiland.mods.commons.util.toImmutable
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import java.util.concurrent.ConcurrentHashMap

/**
 * @author diesieben07
 */
internal class DefaultPacketChannel(override val channel: String, packets: Iterable<RegisteredPacket<*>>) : PacketChannel {

    private val packets = packets.toList().toTypedArray()
    private val packetsByClass = packets.associateBy { it.packetClass }.toImmutable()

    override fun receive(buf: ByteBuf, player: EntityPlayer) {
        val id = buf.readVarInt()
        val packet = packets.getOrNull(id)
        packet?.receive(buf, player) ?: throw ProtocolException("Unknown packetId $id on channel $channel")
    }

    override fun <T : Packet> receiveDirect(packet: T, player: EntityPlayer) {
        @Suppress("UNCHECKED_CAST")
        (packetsByClass[packet.javaClass] as RegisteredPacket<T>).handler(packet, player)
    }
}

internal class DefaultPacketChannelBuilder(channelName: String) : PacketChannel.Builder(channelName) {

    private var nextId = 0
    private val packets = HashMap<Class<out Packet>, RegisteredPacket<*>>()

    override fun <T : Packet> add(packetClass: Class<T>, decoder: ByteBuf.() -> T, handler: T.(EntityPlayer) -> Unit) {
        require(!globalDataStorageMap.containsKey(packetClass) && packets.putIfAbsent(packetClass, RegisteredPacket(nextId, packetClass, decoder, handler)) == null) {
            "Packet class ${packetClass.name} cannot be reused."
        }
        nextId++
    }

    override fun build(): PacketChannel {
        globalDataStorageMap.putAll(packets)
        return DefaultPacketChannel(channelName, packets.values)
    }
}

internal data class RegisteredPacket<T : Packet>(val packetId: Int, val packetClass: Class<T>, val decoder: (ByteBuf) -> T, val handler: T.(EntityPlayer) -> Unit) {

    fun receive(buf: ByteBuf, player: EntityPlayer) {
        decoder(buf).handler(player)
    }
}

private val globalDataStorageMap = ConcurrentHashMap<Class<*>, RegisteredPacket<*>>()

internal object GlobalPacketDataStorage : ClassValue<RegisteredPacket<*>>() {

    override fun computeValue(type: Class<*>): RegisteredPacket<*> {
        return globalDataStorageMap[type] ?: throw IllegalStateException("Packet class ${type.name} was not registered.")
    }
}