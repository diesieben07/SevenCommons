package de.take_weiland.mods.commons.net.packet.raw

import de.take_weiland.mods.commons.net.packet.BasePacketNoResponse
import de.take_weiland.mods.commons.net.packet.PacketReader

abstract class DefaultPacketChannelBuilder(protected val channelName: String) {

    abstract fun build(): PacketChannel

    abstract fun <T : BasePacketNoResponse> add(packetClass: Class<T>, reader: PacketReader<T>)

    inline operator fun <reified T : BasePacketNoResponse> PacketReader<T>.unaryPlus() {
        add(T::class.java, this)
    }

}