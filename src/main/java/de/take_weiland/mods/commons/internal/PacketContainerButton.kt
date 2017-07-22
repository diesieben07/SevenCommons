package de.take_weiland.mods.commons.internal

import de.take_weiland.mods.commons.inv.ButtonContainer
import de.take_weiland.mods.commons.net.packet.Packet
import de.take_weiland.mods.commons.net.readVarInt
import de.take_weiland.mods.commons.net.writeVarInt
import io.netty.buffer.ByteBuf
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

    internal constructor(buf: ByteBuf) {
        this.windowId = buf.readByte().toInt()
        this.buttonId = buf.readVarInt()
    }

    override fun ByteBuf.write() {
        writeByte(windowId)
        writeVarInt(buttonId)
    }

    override fun receive(player: EntityPlayer) {
        if (player.openContainer.windowId == windowId && player.openContainer is ButtonContainer) {
            (player.openContainer as ButtonContainer).onButtonClick(Side.SERVER, player, buttonId)
        }
    }

}
