package de.take_weiland.mods.commons.net.packet.raw

import io.netty.channel.Channel
import io.netty.channel.ChannelPromise

/**
 * @author diesieben07
 */
internal inline fun Channel.write(msg: Any, flush: Boolean = true) {
    if (flush) {
        writeAndFlush(msg)
    } else {
        write(msg)
    }
}

internal inline fun Channel.write(msg: Any, promise: ChannelPromise, flush: Boolean = true) {
    if (flush) {
        writeAndFlush(msg, promise)
    } else {
        write(msg, promise)
    }
}