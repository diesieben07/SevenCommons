package de.take_weiland.mods.commons.internal

import de.take_weiland.mods.commons.inv.Containers
import de.take_weiland.mods.commons.net.MCDataInput
import de.take_weiland.mods.commons.net.MCDataOutput
import de.take_weiland.mods.commons.net.Packet
import de.take_weiland.mods.commons.net.PacketHandler
import de.take_weiland.mods.commons.util.JavaUtils
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.text.ITextComponent
import net.minecraftforge.fml.relauncher.Side

class PacketInventoryName : Packet {

    private val windowId: Int
    private val invIdx: Int
    private val name: ITextComponent

    constructor(windowId: Int, invIdx: Int, name: ITextComponent) {
        this.windowId = windowId
        this.invIdx = invIdx
        this.name = name
    }

    internal constructor(`in`: MCDataInput) {
        windowId = `in`.readByte().toInt()
        invIdx = `in`.readByte().toInt()
        name = ITextComponent.Serializer.jsonToComponent(`in`.readString())
    }

    override fun writeTo(out: MCDataOutput) {
        out.writeByte(windowId)
        out.writeByte(invIdx)
        out.writeString(ITextComponent.Serializer.componentToJson(name))
    }

    @PacketHandler.ReceivingSide(Side.CLIENT)
    fun handle(player: EntityPlayer) {
        if (player.openContainer.windowId == windowId) {
            // TODO
            val inv = JavaUtils.get(Containers.getInventories(player.openContainer).asList(), invIdx)
            //            if (inv instanceof NameableInventory) {
            //                ((NameableInventory) inv).setCustomName(name);
            //            }
        }
    }
}
