package de.take_weiland.mods.commons.net

import com.google.common.base.Utf8
import de.take_weiland.mods.commons.internal.sharedEnumConstants
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.ByteBufOutputStream
import io.netty.buffer.Unpooled
import io.netty.util.CharsetUtil
import net.minecraft.block.Block
import net.minecraft.block.properties.IProperty
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import java.io.DataInput
import java.io.DataOutput
import java.io.InputStream
import java.io.OutputStream
import java.lang.annotation.RetentionPolicy
import java.nio.CharBuffer
import java.nio.channels.GatheringByteChannel
import java.nio.channels.ReadableByteChannel
import java.nio.channels.ScatteringByteChannel
import java.nio.channels.WritableByteChannel
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or

/**
 * @author diesieben07
 */
fun ByteBuf.asInputStream(): InputStream {
    return ByteBufInputStream(this)
}

fun ByteBuf.asOutputStream(): OutputStream {
    return ByteBufOutputStream(this)
}

fun ByteBuf.asDataInput(): DataInput {
    return ByteBufInputStream(this)
}

fun ByteBuf.asDataOutput(): DataOutput {
    return ByteBufOutputStream(this)
}

fun ByteBuf.asReadingByteChannel(): ScatteringByteChannel {
    return ByteBufAsReadingByteChannel(this)
}

fun ByteBuf.asWritingByteChannel(): GatheringByteChannel {
    return ByteBufAsWritingByteChannel(this)
}

var ByteBuf.readerIndex: Int
    inline get() = readerIndex()
    inline set(readerIndex) {
        readerIndex(readerIndex)
    }

var ByteBuf.writerIndex: Int
    inline get() = writerIndex()
    inline set(writerIndex) {
        writerIndex(writerIndex)
    }

inline fun ByteBuf.writeByte(value: Byte) {
    writeByte(value.toInt())
}

inline fun <T> ByteBuf.readNullable(reader: ByteBuf.() -> T): T? {
    if (readByte() == 0.toByte()) {
        return null
    } else {
        return reader()
    }
}

inline fun <T> ByteBuf.writeNullable(value: T?, writer: ByteBuf.(T) -> Unit) {
    if (value == null) {
        writeByte(0)
    } else {
        writeByte(1)
        writer(value)
    }
}

/**
 * Copies `bytes` number of bytes from this buffer into the given output and increments
 * the `readerIndex` accordingly.
 */
fun ByteBuf.readBytes(output: DataOutput, bytes: Int) {
    when {
        hasArray() -> output.write(array(), arrayOffset() + readerIndex(), bytes)
        output is OutputStream -> readBytes(output, bytes)
        else -> {
            val tmp = ByteArray(bytes)
            getBytes(readerIndex(), tmp)
            output.write(tmp)
        }
    }
    readerIndex += bytes
}

/**
 * Copies `bytes` number of bytes from the given input into this buffer and increments
 * the `writerIndex` accordingly.
 */
fun ByteBuf.writeBytes(input: DataInput, bytes: Int) {
    ensureWritable(bytes)
    if (hasArray()) {
        input.readFully(array(), arrayOffset() + writerIndex, bytes)
        writerIndex += bytes
    } else {
        val tmp = ByteArray(bytes)
        input.readFully(tmp)
        writeBytes(tmp)
    }
}

/**
 * Copies up to `bytes` number of bytes from this buffer into the given channel and increments
 * the `readerIndex` accordingly.
 * @return the actual number of bytes copied
 */
fun ByteBuf.readBytes(channel: WritableByteChannel, bytes: Int): Int {
    val nioBuffer = internalNioBuffer(readerIndex(), bytes)
    val copied = channel.write(nioBuffer)
    readerIndex += copied
    return copied
}

/**
 * Copies up to `bytes` number of bytes from the given channel into this buffer.
 * @return the actual number of bytes copied
 */
fun ByteBuf.writeBytes(channel: ReadableByteChannel, bytes: Int = Int.MAX_VALUE): Int {
    val nioBuffer = internalNioBuffer(writerIndex, bytes)
    val copied = Math.max(channel.read(nioBuffer), 0)
    writerIndex += copied
    return copied
}

fun ByteBuf.readVarInt(): Int {
    var result = 0

    var shift = 0
    while (shift < 35) {
        val read = readByte().toInt()
        result = result or ((read and 0x7f) shl shift)

        if (read and 0x80 == 0x80) {
            return result
        }
        shift += 7
    }
    throw RuntimeException("VarInt too big")
}

fun ByteBuf.writeVarInt(value: Int) {
    var i = value
    while (i and 0x7F.inv() != 0) {
        writeByte(i or 0x80)
        i = i ushr 7
    }
    writeByte(i)
}

inline fun ByteBuf.readShiftedVarInt() = readVarInt() - 1

inline fun ByteBuf.writeShiftedVarInt(value: Int) = writeVarInt(value + 1)

fun ByteBuf.readVarLong(): Long {
    var result = 0L

    var shift = 0
    while (shift < 70) {
        val read = readByte().toLong()
        result = result or ((read and 0x7f) shl shift)

        if ((read and 0x80L) == 0x80L) {
            return result
        }

        shift += 7
    }
    throw RuntimeException("VarLong too big")
}

fun ByteBuf.writeVarLong(value: Long) {
    var i = value
    while (i and 0x7FL.inv() != 0L) {
        writeByte((i or 0x80L).toInt())
        i = i ushr 7
    }
    writeByte(i.toInt())
}

inline fun ByteBuf.readShiftedVarLong() = readVarLong() - 1

inline fun ByteBuf.writeShiftedVarLong(value: Long) = writeVarLong(value + 1)

fun ByteBuf.readString(): String {
    val utf8Len = readVarInt()
    if (utf8Len == 0) {
        return ""
    } else {
        val s = toString(readerIndex, utf8Len, StandardCharsets.UTF_8)
        readerIndex += utf8Len
        return s
    }
}

fun ByteBuf.writeString(value: String) {
    val utf8Len = Utf8.encodedLength(value)
    writeVarInt(utf8Len)
    if (utf8Len != 0) {
        // TODO something better?
        val encoder = CharsetUtil.getEncoder(StandardCharsets.UTF_8)
        val dstBuf = internalNioBuffer(writerIndex, utf8Len)

        var cr = encoder.encode(CharBuffer.wrap(value), dstBuf, true)

        if (!cr.isUnderflow) {
            cr.throwException()
        }
        cr = encoder.flush(dstBuf)
        if (!cr.isUnderflow) {
            cr.throwException()
        }
        writerIndex += utf8Len
    }
}

fun ByteBuf.readUUID(): UUID {
    return UUID(readLong(), readLong())
}

fun ByteBuf.writeUUID(value: UUID) {
    writeLong(value.mostSignificantBits)
    writeLong(value.leastSignificantBits)
}

inline fun <reified E : Enum<E>> ByteBuf.readEnum(): E {
    return readEnum(E::class.java)
}

fun <E : Enum<E>> ByteBuf.readEnum(cls: Class<E>): E {
    return cls.enumConstants[readVarInt()]
}

fun <E : Enum<E>> ByteBuf.writeEnum(value: E) {
    writeVarInt(value.ordinal)
}

fun ByteBuf.readBitSet(): BitSet {
    val len = readVarInt()
    return BitSet.valueOf(internalNioBuffer(readerIndex, len))
}

fun ByteBuf.writeBitSet(value: BitSet) {
    val bytes = value.toByteArray()
    writeVarInt(bytes.size)
    writeBytes(bytes)
}

inline fun <reified E : Enum<E>> ByteBuf.readEnumSet(): EnumSet<E> {
    return readEnumSet(E::class.java)
}

fun <E : Enum<E>> ByteBuf.readEnumSet(cls: Class<E>) : EnumSet<E> {
    val universe = cls.sharedEnumConstants
    val universeSize = universe.size
    require(universeSize > 0)

    val set = EnumSet.noneOf(cls)

    val byteCount = (universeSize + 7) / 8
    var i = 0
    while (i < byteCount) {
        val b = readByte()
        var bit = 0
        while (bit < 8) {
            if (b and (1 shl bit).toByte() != 0.toByte()) {
                set.add(universe[(i shl 3) or bit])
            }
            bit++
        }
        i++
    }
    return set
}

inline fun <reified E : Enum<E>> ByteBuf.writeEnumSet(value: EnumSet<E>) {
    writeEnumSet(E::class.java, value)
}

fun <E : Enum<E>> ByteBuf.writeEnumSet(cls: Class<E>, value: EnumSet<E>) {
    val universe = cls.sharedEnumConstants
    val universeSize = universe.size
    require(universeSize > 0)

    val byteCount = (universeSize + 7) shr 3 // divide by 8 and round up
    var i = 0
    while (i < byteCount) {
        var b = 0.toByte()
        var bit = 0
        val end = if (byteCount - i == 1) universeSize and 0b1111_1111 else 8
        while (bit < end) {
            if (value.contains(universe[(i shl 3) or bit])) {
                b = b or (1 shl bit).toByte()
            }
            bit++
        }
        writeByte(b)
        i++
    }
}



// Minecraft types

fun ByteBuf.readChunkPos(): ChunkPos {
    return ChunkPos(readMedium(), readMedium())
}

fun ByteBuf.writeChunkPos(value: ChunkPos) {
    writeMedium(value.chunkXPos)
    writeMedium(value.chunkZPos)
}

fun ByteBuf.readBlockPos(): BlockPos {
    return BlockPos.fromLong(readLong())
}

fun ByteBuf.writeBlockPos(value: BlockPos) {
    writeLong(value.toLong())
}

fun ByteBuf.readBlockState(): IBlockState {
    return Block.getStateById(readShort().toInt())
}

fun ByteBuf.writeBlockState(value: IBlockState) {
    writeShort(Block.getStateId(value))
}

fun ByteBuf.readRichBlockState(): IBlockState {
    var state = readBlockState()
    val stateContainer = state.block.blockState
    do {
        val propertyName = readNullable { readString() } ?: return state
        val prop = stateContainer.getProperty(propertyName)
        prop?.let { state = readPropertyValue(state, it as IProperty<Any>) }
    } while (true)
}

private fun <T : Comparable<T>> ByteBuf.readPropertyValue(state: IBlockState, property: IProperty<T>): IBlockState {
    return property.parseValue(readString()).orNull()?.let { state.withProperty(property, it) } ?: state
}

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
private inline fun <E : Enum<E>> ByteBuf.readEnumUnchecked(valueClass : Class<*>): E {
    return readEnum(valueClass as Class<E>)
}

fun main(args: Array<String>) {
    val buf = Unpooled.buffer()
    val set = EnumSet.of(RetentionPolicy.RUNTIME, RetentionPolicy.SOURCE)

    buf.writeEnumSet(set)
    println(buf.readEnumSet<RetentionPolicy>())
}


