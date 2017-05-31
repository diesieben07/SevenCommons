package de.take_weiland.mods.commons.net.packet

import de.take_weiland.mods.commons.net.readString
import de.take_weiland.mods.commons.net.writeString
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer

class NamePacket(val name: String) : Packet {

    override fun ByteBuf.write() {
        writeString(name)
    }

    override fun receive(player: EntityPlayer) {
        println("received $name for $player")
    }
}

fun ByteBuf.readNamePacket() = NamePacket(name = readString())

fun main(args: Array<String>) {

}