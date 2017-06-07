package de.take_weiland.mods.commons.net.packet

import de.take_weiland.mods.commons.net.readString
import de.take_weiland.mods.commons.net.writeString
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer

class NamePacket(val name: String) : Packet.Async {

    override fun ByteBuf.write() {
        writeString(name)
    }

    companion object Reader : PacketReader<NamePacket> {
        override fun ByteBuf.read() = NamePacket(name = readString())
    }

    override fun receive(player: EntityPlayer?) {
        println("received $name for $player")
    }

}

fun main(args: Array<String>) {
    val bla = ByteBuf::writeBoolean

    println(bla)

    val list = listOf(1, 2, 3)
    (list as MutableList<Int>)[2] = 4
    println(list)
}