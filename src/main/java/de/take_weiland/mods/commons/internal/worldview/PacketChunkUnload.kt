package de.take_weiland.mods.commons.internal.worldview

import de.take_weiland.mods.commons.net.MCDataInput
import de.take_weiland.mods.commons.net.MCDataOutput
import de.take_weiland.mods.commons.net.Packet

/**
 * @author diesieben07
 */
class PacketChunkUnload : Packet {

    val dimension: Int
    val chunkX: Int
    val chunkZ: Int

    constructor(dimension: Int, chunkX: Int, chunkZ: Int) {
        this.dimension = dimension
        this.chunkX = chunkX
        this.chunkZ = chunkZ
    }

    constructor(`in`: MCDataInput) {
        this.dimension = `in`.readVarInt()
        this.chunkX = `in`.readInt()
        this.chunkZ = `in`.readInt()
    }

    @Throws(Exception::class)
    override fun writeTo(out: MCDataOutput) {
        out.writeVarInt(dimension)
        out.writeInt(chunkX)
        out.writeInt(chunkZ)
    }
}
