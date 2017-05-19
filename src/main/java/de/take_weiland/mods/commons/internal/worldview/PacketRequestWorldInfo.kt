package de.take_weiland.mods.commons.internal.worldview

import de.take_weiland.mods.commons.net.MCDataInput
import de.take_weiland.mods.commons.net.MCDataOutput
import de.take_weiland.mods.commons.net.Packet
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.DimensionManager

/**
 * @author diesieben07
 */
class PacketRequestWorldInfo : Packet.WithResponse<PacketWorldInfo> {

    private val dimension: Int

    constructor(dimension: Int) {
        this.dimension = dimension
    }

    constructor(`in`: MCDataInput) {
        this.dimension = `in`.readInt()
    }

    @Throws(Exception::class)
    override fun writeTo(out: MCDataOutput) {
        out.writeInt(dimension)
    }

    fun handle(): PacketWorldInfo {
        val world = DimensionManager.getWorld(dimension)
        if (world == null) {
            // todo
            return PacketWorldInfo(BlockPos.ORIGIN, 0, 0)
        } else {
            val spawn = world.spawnPoint
            return PacketWorldInfo(spawn, world.skylightSubtracted, world.worldTime)
        }
    }
}
