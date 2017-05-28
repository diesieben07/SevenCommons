package de.take_weiland.mods.commons.net.mod

import de.take_weiland.mods.commons.net.readString
import de.take_weiland.mods.commons.net.simple.sendTo
import de.take_weiland.mods.commons.net.writeString
import io.netty.buffer.ByteBuf
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.future.future
import net.minecraft.entity.player.EntityPlayer

/**
 * @author diesieben07
 */
interface PacketChannel {

    val channel: String

    fun receive(buf: ByteBuf, player: EntityPlayer)

    fun <T : Packet> receiveDirect(packet: T, player: EntityPlayer)

    abstract class Builder(protected val channelName: String) {

        abstract fun build(): PacketChannel

        abstract fun <T : Packet> add(packetClass: Class<T>, decoder: ByteBuf.() -> T, handler: T.(EntityPlayer) -> Unit)

        inline infix fun <reified T : Packet> ((ByteBuf) -> T).with(noinline handler: T.(EntityPlayer) -> Unit) {
            add(T::class.java, this, handler)
        }

    }

    companion object {

        inline operator fun invoke(channelName: String, body: PacketChannel.Builder.() -> Unit) {
            builder(channelName).body()
        }

        fun builder(channelName: String): PacketChannel.Builder {
            return DefaultPacketChannelBuilder(channelName)
        }

    }

}

class TestResponse : Packet.Response {

    val text: String

    constructor(text: String) {
        this.text = text
    }

    constructor(input: ByteBuf) {
        this.text = input.readString()
    }

    override fun ByteBuf.writeTo() {
        writeString(text)
    }

}

class TestPacket : Packet.WithResponse<TestResponse> {

    val name: String

    constructor(input: ByteBuf) {
        this.name = input.readString()
    }

    constructor(name: String) {
        this.name = name
    }

    override fun ByteBuf.writeTo() {
        writeString(name)
    }

    override fun receive(player: EntityPlayer): TestResponse {
        return TestResponse(name.toLowerCase())
    }
}

fun main(args: Array<String>) {
    val player: EntityPlayer = TODO()

    future {
        val response: TestResponse = TestPacket("hello").sendTo(player).toCompletableFuture()
                .await()

    }


}