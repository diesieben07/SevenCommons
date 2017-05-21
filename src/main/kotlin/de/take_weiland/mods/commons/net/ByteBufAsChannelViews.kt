package de.take_weiland.mods.commons.net

import com.google.common.primitives.Ints
import io.netty.buffer.ByteBuf
import java.nio.ByteBuffer
import java.nio.channels.Channel
import java.nio.channels.GatheringByteChannel
import java.nio.channels.ScatteringByteChannel

/**
 * @author diesieben07
 */
internal abstract class BaseAsChannelView(protected val buf: ByteBuf) : Channel {

    final override fun isOpen() = true

    final override fun close() = Unit
}


internal class ByteBufAsReadingByteChannel(src: ByteBuf) : BaseAsChannelView(src), ScatteringByteChannel {

    private val src inline get() = buf
    
    override fun read(dst: ByteBuffer): Int {
        synchronized(src) {
            return readUnsynchronized(dst)
        }
    }

    private fun readUnsynchronized(dst: ByteBuffer): Int {
        val available = src.readableBytes()
        if (available == 0) {
            return -1
        }
        val toTransfer = Math.min(dst.remaining(), available)
        if (src.hasArray()) {
            dst.put(src.array(), src.arrayOffset() + src.readerIndex(), toTransfer)
        } else {
            dst.put(src.nioBuffer())
        }

        src.readerIndex(src.readerIndex() + toTransfer)

        return toTransfer
    }

    override fun read(dsts: Array<out ByteBuffer>) = read(dsts, 0, dsts.size)

    override fun read(dsts: Array<out ByteBuffer>, offset: Int, length: Int): Long {
        synchronized(src) {
            if (!src.isReadable) {
                return -1
            }

            var read: Long = 0
            var i = 0
            while (i < length) {
                val r = readUnsynchronized(dsts[i + offset])
                if (r < 0) break
                read += r.toLong()

                i++
            }

            return read
        }
    }

}

internal class ByteBufAsWritingByteChannel(buf: ByteBuf) : BaseAsChannelView(buf), GatheringByteChannel {

    private val dst inline get() = buf

    override fun write(src: ByteBuffer): Int {
        synchronized(dst) {
            val n = src.remaining()
            dst.ensureWritable(n)
            dst.writeBytes(src)
            return n
        }
    }

    override fun write(srcs: Array<out ByteBuffer>) = write(srcs, 0, srcs.size)

    override fun write(srcs: Array<out ByteBuffer>, offset: Int, length: Int): Long {
        synchronized(dst) {
            var n = 0L
            var i = 0
            while (i < length) {
                n += srcs[i].remaining()
                i++
            }

            val ni = Ints.saturatedCast(n)
            dst.ensureWritable(ni)

            i = 0
            while (i < length) {
                dst.writeBytes(srcs[i])
            }

            return ni.toLong()
        }
    }
}