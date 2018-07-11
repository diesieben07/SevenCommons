package de.takeweiland.mods.commons

import de.takeweiland.mods.commons.net.Packet
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer

/**
 * @author Take Weiland
 */
class TestPacket : Packet {

    private val foo: Int

    constructor(foo: Int) : super() {
        this.foo = foo
    }

    constructor(buf: ByteBuf) : super() {
        this.foo = buf.readInt()
    }

    override fun write(buf: ByteBuf) {
        buf.writeInt(foo)
    }

    override fun process(player: EntityPlayer) {
        println("handling packet: $this")
    }

}