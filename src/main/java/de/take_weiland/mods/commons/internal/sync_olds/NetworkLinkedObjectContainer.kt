package de.take_weiland.mods.commons.internal.sync_olds

import de.take_weiland.mods.commons.net.MCDataInput
import de.take_weiland.mods.commons.net.MCDataOutput
import de.take_weiland.mods.commons.net.simple.SimplePacket
import de.take_weiland.mods.commons.sync.NetworkLinkedObjectType
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Container

/**
 * @author diesieben07
 */
class NetworkLinkedObjectContainer : NetworkLinkedObjectType<Container, Int> {
    override fun write(`object`: Container, out: MCDataOutput) {
        out.writeByte(`object`.windowId)
    }

    override fun read(`in`: MCDataInput, player: EntityPlayer): Container? {
        val windowId = `in`.readInt()
        return getObject(windowId, player)
    }

    override fun getData(`object`: Container): Int? {
        return `object`.windowId
    }

    override fun getObject(windowId: Int?, player: EntityPlayer): Container? {
        return if (player.openContainer.windowId == windowId) player.openContainer else null
    }

    override fun sendToTracking(obj: Container, packet: SimplePacket) {
//        packet.sendToTracking(obj)
    }
}
