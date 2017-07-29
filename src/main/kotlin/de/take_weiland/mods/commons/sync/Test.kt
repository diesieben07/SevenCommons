package de.take_weiland.mods.commons.sync

import de.take_weiland.mods.commons.util.isServer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ITickable
import net.minecraft.util.text.TextComponentString
import java.util.*

/**
 * @author diesieben07
 */

open class Test : TileEntity(), ITickable {

    var bla: Int by sync(0)

    var tick = 0

    override fun update() {
        tick++
        if (world.totalWorldTime.rem(100) == 0L) {
            if (isServer) {
                bla = Random().nextInt(30)
                for (player in world.playerEntities) {
                    player.sendStatusMessage(TextComponentString("update: $bla"), false)
                }
            } else {
                for (player in world.playerEntities) {
                    player.sendStatusMessage(TextComponentString("client: $bla"), false)
                }
            }
        }
    }
}
