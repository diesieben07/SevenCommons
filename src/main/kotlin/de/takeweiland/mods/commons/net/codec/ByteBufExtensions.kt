package de.takeweiland.mods.commons.net.codec

import io.netty.buffer.ByteBuf
import net.minecraft.network.PacketBuffer
import java.util.*

var ByteBuf.readerIndex
    inline get() = readerIndex()
    inline set(value) {
        readerIndex(value)
    }

var ByteBuf.writerIndex
    inline get() = writerIndex()
    inline set(value) {
        writerIndex(value)
    }

fun ByteBuf.writeVarInt(n: Int) {
    var r = n
    while (r and 0b0111_1111.inv() != 0) {
        writeByte(r and 0b0111_1111 or 0b1000_0000)
        r = r ushr 7
    }
    writeByte(r)
}

fun ByteBuf.readVarInt(): Int {
    var result = 0
    var shift = 0
    do {
        val r = readByte().toInt()
        result = result or (r and 0b0111_1111).shl(shift)
        shift += 7
    } while ((r and 0b1000_0000) != 0)

    return result
}

/**
 * Write a "small string" in a way compatible with [PacketBuffer.readString], but more efficient than [PacketBuffer.writeString].
 * The strings written by this method must only contain ASCII characters and must not exceed a length of 127.
 */
internal fun ByteBuf.writeVanillaCompatibleSmallString(s: String) {
    writeZero(0) // reserve space for the length byte
    val wi = writerIndex
    writeCharSequence(s, Charsets.US_ASCII)
    val byteLen = writerIndex - wi
    assert(byteLen <= 127) { "String too big." }
    setByte(wi - 1, byteLen)
}

internal fun ByteBuf.readVanillaCompatibleString(): String {
    val byteLen = readVarInt()
    val s = toString(readerIndex, byteLen, Charsets.UTF_8)
    readerIndex += byteLen
    return s
}

fun ByteBuf.writeString(s: String) {
    writeZero(4)
    val wi = writerIndex
    writeCharSequence(s, Charsets.UTF_8)
    val byteLen = writerIndex - wi
    setInt(wi - 4, byteLen)
}

fun ByteBuf.readString(): String {
    val byteLen = readInt()
    val result = toString(readerIndex(), byteLen, Charsets.UTF_8)
    readerIndex += byteLen
    return result
}

fun ByteBuf.writeUUID(uuid: UUID) {
    writeLong(uuid.leastSignificantBits)
    writeLong(uuid.mostSignificantBits)
}

fun ByteBuf.readUUID(): UUID {
    val lsb = readLong()
    val msb = readLong()
    return UUID(msb, lsb)
}