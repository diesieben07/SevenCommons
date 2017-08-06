package de.take_weiland.mods.commons.net.packet.mod

import io.netty.buffer.ByteBuf

interface PacketReader<out T : BaseModPacket> : (ByteBuf) -> T {

    fun ByteBuf.read(): T

    override fun invoke(p1: ByteBuf): T = p1.read()
}