package de.takeweiland.mods.commons

import de.takeweiland.mods.commons.net.PacketWithResponse
import de.takeweiland.mods.commons.net.ResponsePacket
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer

/**
 * @author Take Weiland
 */
class TestPacketWithResponse : PacketWithResponse<TestPacketWithResponse.Response> {

    private val number: Int

    constructor(number: Int) {
        this.number = number
    }

    constructor(buf: ByteBuf) {
        this.number = buf.readInt()
    }

    override fun write(buf: ByteBuf) {
        buf.writeInt(this.number)
    }

    override fun process(player: EntityPlayer): Response {
        println("server send to client: $number")
        return Response(number + 1)
    }

    class Response : ResponsePacket {

        val number: Int

        constructor(number: Int) {
            this.number = number
        }

        constructor(buf: ByteBuf) {
            this.number = buf.readInt()
        }

        override fun write(buf: ByteBuf) {
            buf.writeInt(number)
        }
    }

}