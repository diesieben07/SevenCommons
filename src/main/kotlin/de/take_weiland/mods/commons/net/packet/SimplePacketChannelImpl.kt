package de.take_weiland.mods.commons.net.packet

import de.take_weiland.mods.commons.net.ProtocolException
import de.take_weiland.mods.commons.net.packet.mod.BaseModPacket
import de.take_weiland.mods.commons.net.packet.mod.ModPacketNoResponse
import de.take_weiland.mods.commons.net.packet.mod.ModPacketWithResponse
import de.take_weiland.mods.commons.net.packet.mod.Packet
import de.take_weiland.mods.commons.net.packet.raw.PacketChannel
import de.take_weiland.mods.commons.net.readVarInt
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.relauncher.Side
import java.util.concurrent.ConcurrentHashMap

/**
 * @author diesieben07
 */
internal class SimplePacketChannelImpl(override val channel: String, packets: Iterable<RegisteredPacket<*>>) : PacketChannel {

    private val packets = packets.sortedBy { it.packetId }.toTypedArray()

    override fun receive(buf: ByteBuf, side: Side, player: EntityPlayer?) {
        val id = buf.readPacketId()
        val packet = packets.getOrNull(id)
        packet?.receive(buf, side, player) ?: throw ProtocolException("Unknown packetId $id on channel $channel")
    }

}

internal class SimplePacketChannelBuilderImpl(channelName: String) : SimplePacketChannelBuilder(channelName) {

    private var nextId = 0
    private val packets = HashMap<Class<out BaseModPacket>, RegisteredPacket<*>>()

    private fun checkReuse(clazz: Class<out BaseModPacket>) {
        require(clazz !in globalDataStorageMap && clazz !in packets) {
            "Packet class ${clazz.name} cannot be reused."
        }
    }

    override fun <T : ModPacketNoResponse> packet(packetClass: Class<T>, reader: (ByteBuf) -> T) {
        checkReuse(packetClass)
        packets.put(packetClass, RegisteredPacket.NoResponse(nextId, channelName, packetClass, reader))
        nextId++
    }

    override fun <T : ModPacketWithResponse<R>, R : Packet.Response> packet(packetClass: Class<T>, reader: (ByteBuf) -> T, responseClass: Class<R>, responseReader: (ByteBuf) -> R) {
        checkReuse(packetClass)
        checkReuse(responseClass)

        val packetId = nextId++
        val responseId = nextId++

        packets.put(packetClass, RegisteredPacket.WithResponse(packetId, channelName, packetClass, reader, responseClass, responseReader))
        packets.put(responseClass, RegisteredPacket.Response(responseId, channelName, responseClass, responseReader))
    }

    override fun create(): PacketChannel {
        for ((clazz, pkt) in packets) {
            globalDataStorageMap
        }
        globalDataStorageMap.putAll(packets)
        return SimplePacketChannelImpl(channelName, packets.values).also {
            PacketChannel.register(it)
        }
    }
}

internal abstract class RegisteredPacket<T : BaseModPacket>(@JvmField val packetId: Int,
                                                            @JvmField val channel: String,
                                                            @JvmField val packetClass: Class<T>,
                                                            @JvmField val reader: (ByteBuf) -> T) {

    abstract fun receive(buf: ByteBuf, side: Side, player: EntityPlayer?)

    class NoResponse<T : ModPacketNoResponse>(packetId: Int,
                                              channel: String,
                                              packetClass: Class<T>,
                                              reader: (ByteBuf) -> T) : RegisteredPacket<T>(packetId, channel, packetClass, reader) {

        override fun receive(buf: ByteBuf, side: Side, player: EntityPlayer?) {
            reader(buf).receiveAfterReconstruction(side, player)
        }
    }

    class WithResponse<T : ModPacketWithResponse<R>, R : Packet.Response>(packetId: Int,
                                                                          channel: String,
                                                                          packetClass: Class<T>,
                                                                          reader: (ByteBuf) -> T,
                                                                          @JvmField val responseClass: Class<R>,
                                                                          @JvmField val responseReader: (ByteBuf) -> R) : RegisteredPacket<T>(packetId, channel, packetClass, reader) {

        override fun receive(buf: ByteBuf, side: Side, player: EntityPlayer?) {
            val responseId = buf.readVarInt()
            reader(buf).receiveAfterReconstruction(responseId, side, player)
        }
    }

    class Response<T : ModPacketWithResponse<R>, R : Packet.Response>(packetId: Int,
                                                                      channel: String,
                                                                      packetClass: Class<R>,
                                                                      reader: (ByteBuf) -> R) : RegisteredPacket<R>(packetId, channel, packetClass, reader) {
        override fun receive(buf: ByteBuf, side: Side, player: EntityPlayer?) {
            val responseId = buf.readVarInt()
            val response = reader(buf)
            resolvePendingResponse(side, channel, responseId, response)
        }
    }
}



private val globalDataStorageMap = ConcurrentHashMap<Class<out BaseModPacket>, RegisteredPacket<*>>()

internal object GlobalPacketDataStorage : ClassValue<RegisteredPacket<*>>() {

    override fun computeValue(type: Class<*>): RegisteredPacket<*> {
        return globalDataStorageMap[type] ?: throw IllegalStateException("Packet class ${type.name} was not registered.")
    }

}

@Suppress("UNCHECKED_CAST")
internal val <T : BaseModPacket> T.data: RegisteredPacket<T>
    inline get() = GlobalPacketDataStorage[javaClass] as RegisteredPacket<T>