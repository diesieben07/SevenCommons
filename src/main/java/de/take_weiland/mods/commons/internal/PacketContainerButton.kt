package de.take_weiland.mods.commons.internal

import de.take_weiland.mods.commons.inv.ButtonContainer
import de.take_weiland.mods.commons.net.MCDataInput
import de.take_weiland.mods.commons.net.MCDataOutput
import de.take_weiland.mods.commons.net.Packet
import de.take_weiland.mods.commons.net.PacketHandler
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.relauncher.Side

/**
 * @author diesieben07
 */
class PacketContainerButton : Packet {

    private val windowId: Int
    private val buttonId: Int

    constructor(windowId: Int, buttonId: Int) {
        this.windowId = windowId
        this.buttonId = buttonId
    }

    internal constructor(buf: MCDataInput) {
        this.windowId = buf.readByte().toInt()
        this.buttonId = buf.readVarInt()
    }

    override fun writeTo(out: MCDataOutput) {
        out.writeByte(windowId)
        out.writeVarInt(buttonId)
    }

    @PacketHandler.ReceivingSide(Side.SERVER)
    fun handle(player: EntityPlayer) {
        if (player.openContainer.windowId == windowId && player.openContainer is ButtonContainer) {
            (player.openContainer as ButtonContainer).onButtonClick(Side.SERVER, player, buttonId)
        }
    }
}
