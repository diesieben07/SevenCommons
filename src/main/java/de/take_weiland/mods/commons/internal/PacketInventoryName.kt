package de.take_weiland.mods.commons.internal

import de.take_weiland.mods.commons.net.packet.Packet
import de.take_weiland.mods.commons.net.readString
import de.take_weiland.mods.commons.net.readVarInt
import de.take_weiland.mods.commons.net.writeString
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentString

class PacketInventoryName : Packet {

    private val windowId: Int
    private val slotIndex: Int
    private val name: ITextComponent

    constructor(windowId: Int, invIdx: Int, name: ITextComponent) {
        this.windowId = windowId
        this.slotIndex = invIdx
        this.name = name
    }

    internal constructor(input: ByteBuf) {
        windowId = input.readByte().toInt()
        slotIndex = input.readVarInt()
        name = ITextComponent.Serializer.jsonToComponent(input.readString()) ?: TextComponentString("")
    }

    override fun ByteBuf.write() {
        writeByte(windowId)
        writeByte(slotIndex)
        writeString(ITextComponent.Serializer.componentToJson(name))
    }

    override fun receive(player: EntityPlayer) {
        if (player.openContainer.windowId == windowId) {
            // TODO
            val inv = player.openContainer.inventorySlots.getOrNull(slotIndex)?.let { inv ->

            }
            //            if (inv instanceof NameableInventory) {
            //                ((NameableInventory) inv).setCustomName(name);
            //            }
        }
    }

}
