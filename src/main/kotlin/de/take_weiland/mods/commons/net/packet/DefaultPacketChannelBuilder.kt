package de.take_weiland.mods.commons.net.packet.raw

import de.take_weiland.mods.commons.net.packet.Packet
import io.netty.buffer.ByteBuf

abstract class DefaultPacketChannelBuilder(protected val channelName: String) {

    abstract fun build(): PacketChannel

    abstract fun <T : Packet> add(packetClass: Class<T>, decoder: ByteBuf.() -> T)

    inline operator fun <reified T : Packet> (ByteBuf.() -> T).unaryPlus() {
        add(T::class.java, this)
    }

}