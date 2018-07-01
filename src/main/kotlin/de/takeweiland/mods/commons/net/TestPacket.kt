package de.takeweiland.mods.commons.net

import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.text.TextComponentString

/**
 * @author Take Weiland
 */
class TestPacket : Packet {

    private val foo: Int

    constructor(foo: Int) : super() {
        this.foo = foo
    }

    constructor(buf: ByteBuf) : this(buf.readInt())

    override val channel: String
        get() = "sevencommons"

    override fun process(player: EntityPlayer) {
        player.sendMessage(TextComponentString(foo.toString()))
    }

    override fun write(buf: ByteBuf) {
        buf.writeInt(foo)
    }
}