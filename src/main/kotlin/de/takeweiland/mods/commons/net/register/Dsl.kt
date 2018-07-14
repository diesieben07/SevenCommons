package de.takeweiland.mods.commons.net.register

import de.takeweiland.mods.commons.net.AnyPacketWithResponse
import de.takeweiland.mods.commons.net.Packet
import de.takeweiland.mods.commons.net.ResponsePacket
import io.netty.buffer.ByteBuf

/**
 * @author Take Weiland
 */
abstract class ChannelBuilderDslContext internal constructor() : ChannelBuilderBase {

    inline fun <reified P : Packet> packet(id: Int, noinline constructor: (ByteBuf) -> P) {
        add(id, P::class.java, constructor)
    }

    inline fun <reified P : AnyPacketWithResponse<R>, reified R : ResponsePacket> packet(
        id: Int, noinline constructor: (ByteBuf) -> P, noinline responseConstructor: (ByteBuf) -> R
    ) {
        add(id, P::class.java, R::class.java, constructor, responseConstructor)
    }
}

fun networkChannel(channel: String, body: ChannelBuilderDslContext.() -> Unit) {
    val builder = ChannelBuilderImpl(channel)
    builder.body()
    builder.register()
}