package de.take_weiland.mods.commons.internal

import de.take_weiland.mods.commons.net.packet.mod.Packet
import de.take_weiland.mods.commons.net.readString
import de.take_weiland.mods.commons.net.readVarInt
import de.take_weiland.mods.commons.net.writeString
import io.netty.buffer.ByteBuf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TextComponentString
import net.minecraftforge.fml.relauncher.Side

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

    override fun write(buf: ByteBuf) {
        buf.writeByte(this.windowId)
        buf.writeByte(this.slotIndex)
        buf.writeString(ITextComponent.Serializer.componentToJson(this.name))
    }

    override fun receive(side: Side, player: EntityPlayer) {
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
