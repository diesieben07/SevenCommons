package de.take_weiland.mods.commons.internal.worldview

import de.take_weiland.mods.commons.net.MCDataInput
import de.take_weiland.mods.commons.net.MCDataOutput
import de.take_weiland.mods.commons.net.Packet

/**
 * @author diesieben07
 */
class PacketBlockChange : Packet {

    val x: Int
    val y: Int
    val z: Int
    val dimension: Int
    val data: Int

    constructor(dimension: Int, x: Int, y: Int, z: Int, block: Int, metadata: Int) {
        this.x = x
        this.y = y
        this.z = z
        this.dimension = dimension
        this.data = block shl 4 or metadata
    }

    @Throws(Exception::class)
    constructor(`in`: MCDataInput) {
        this.x = `in`.readInt()
        this.y = `in`.readUnsignedByte()
        this.z = `in`.readInt()
        this.dimension = `in`.readVarInt()
        this.data = `in`.readUnsignedShort()
    }

    @Throws(Exception::class)
    override fun writeTo(out: MCDataOutput) {
        out.writeInt(x)
        out.writeByte(y)
        out.writeInt(z)
        out.writeVarInt(dimension)
        out.writeShort(data)
    }

}
