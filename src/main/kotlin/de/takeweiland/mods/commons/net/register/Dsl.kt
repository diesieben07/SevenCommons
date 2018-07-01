package de.takeweiland.mods.commons.net.register

import de.takeweiland.mods.commons.net.Packet
import io.netty.buffer.ByteBuf

/**
 * @author Take Weiland
 */
abstract class ChannelBuilderDslContext internal constructor(): ChannelBuilderBase {

    inline operator fun <reified P : Packet> Int.rem(noinline constructor: (ByteBuf) -> P) {
        add(this, P::class.java, constructor)
    }

}

fun networkChannel(channel: String, body: ChannelBuilderDslContext.() -> Unit) {
    val builder = ChannelBuilderImpl()
    builder.body()

}