package de.takeweiland.mods.commons.net.registry

import de.takeweiland.mods.commons.net.*
import io.netty.buffer.ByteBuf

/**
 * @author Take Weiland
 */
internal var globalNetworkChannels: MutableNetworkChannelRegistry = MutableNetworkChannelRegistryImpl()

private val registrationLock = Any()
private var registrationsFrozen = false
private var registrations: MutableList<SimplePacketData<*>> = ArrayList()

internal object GlobalPacketRegistry : ClassValue<SimplePacketData<*>>() {

    override fun computeValue(type: Class<*>): SimplePacketData<*> {
        return synchronized(registrationLock) {
            val matches = registrations.filter { type in it.packetClasses }
            when (matches.size) {
                0 -> throw IllegalArgumentException("Unknown packet class ${type.name}")
                1 -> matches[0]
                else -> throw IllegalArgumentException("Packet class ${type.name} has multiple data instances associated with it. This should be impossible.")
            }
        }
    }
}

private fun register(data: SimplePacketData<*>) {
    synchronized(registrationLock) {
        check(!registrationsFrozen) {
            "Packet registrations are frozen. Packet registration must occur during the preInit phase."
        }
        require(registrations.none { it.channel == data.channel && it.id == data.id }) {
            "Duplicate packet ID ${data.id} for channel ${data.channel}"
        }
        require(registrations.all { reg ->
            data.packetClasses.none { dpc -> reg.packetClasses.contains(dpc) }
        })
        registrations.add(data)
    }
}

internal fun freezePacketRegistry() {
    synchronized(registrationLock) {
        registrationsFrozen = true
    }
}

internal fun <T : Packet> registerPlainPacket(channel: String, id: Int, cls: Class<T>, factory: (ByteBuf) -> T): SimplePacketData<*> {
    return SimplePacketData.Plain(channel, id, cls, factory).also { register(it) }
}

internal fun <T : PacketWithResponse<R>, R : ResponsePacket> registerResponsePacket(
    channel: String, id: Int, cls: Class<T>, responseClass: Class<R>, factory: (ByteBuf) -> T, responseFactory: (ByteBuf) -> R
): SimplePacketData<*> {
    return SimplePacketData.WithResponse(channel, id, cls, responseClass, factory, responseFactory).also { register(it) }
}

internal fun <T : Packet> getPlainPacketData(packetClass: Class<T>): SimplePacketData.Plain<T> {
    @Suppress("UNCHECKED_CAST")
    return getAnyPacketData(packetClass) as SimplePacketData.Plain<T>
}

internal fun <T : AnyPacketWithResponse<R>, R : ResponsePacket> getResponsePacketData(packetClass: Class<T>): SimplePacketData.WithResponse<T, R> {
    @Suppress("UNCHECKED_CAST")
    return getAnyPacketData(packetClass) as SimplePacketData.WithResponse<T, R>
}

@JvmName("getResponsePacketDataFromResponseClass")
internal fun <R : ResponsePacket> getResponsePacketData(packetClass: Class<R>): SimplePacketData.WithResponse<*, R> {
    @Suppress("UNCHECKED_CAST")
    return getAnyPacketData(packetClass) as SimplePacketData.WithResponse<*, R>
}

private fun <T : PacketBase> getAnyPacketData(packetClass: Class<T>): SimplePacketData<*> = GlobalPacketRegistry.get(packetClass)

sealed class SimplePacketData<T : PacketBase> {

    abstract val packetClasses: List<Class<out PacketBase>>
    abstract val channel: String
    abstract val id: Int

    data class Plain<T : Packet>(
        override val channel: String, override val id: Int, val packetClass: Class<T>,
        val factory: (ByteBuf) -> T
    ) : SimplePacketData<T>() {
        override val packetClasses: List<Class<out PacketBase>>
            get() = listOf(packetClass)
    }

    data class WithResponse<T : AnyPacketWithResponse<R>, R : ResponsePacket>(
        override val channel: String,
        override val id: Int,
        val packetClass: Class<T>, val responseClass: Class<R>,
        val factory: (ByteBuf) -> T, val responseFactory: (ByteBuf) -> R
    ) : SimplePacketData<T>() {
        override val packetClasses: List<Class<out PacketBase>>
            get() = listOf(packetClass, responseClass)
    }

}