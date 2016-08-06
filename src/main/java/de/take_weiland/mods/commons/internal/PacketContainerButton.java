package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.inv.ButtonContainer;
import de.take_weiland.mods.commons.net.MCDataInput;
import de.take_weiland.mods.commons.net.MCDataOutput;
import de.take_weiland.mods.commons.net.Packet;
import de.take_weiland.mods.commons.net.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

/**
 * @author diesieben07
 */
public final class PacketContainerButton implements Packet {

    private final int windowId;
    private final int buttonId;

    public PacketContainerButton(int windowId, int buttonId) {
        this.windowId = windowId;
        this.buttonId = buttonId;
    }

    PacketContainerButton(MCDataInput buf) {
        this.windowId = buf.readByte();
        this.buttonId = buf.readVarInt();
    }

    @Override
    public void writeTo(MCDataOutput out) {
        out.writeByte(windowId);
        out.writeVarInt(buttonId);
    }

    @PacketHandler.ReceivingSide(Side.SERVER)
    void handle(EntityPlayer player) {
        if (player.openContainer.windowId == windowId && player.openContainer instanceof ButtonContainer) {
            ((ButtonContainer) player.openContainer).onButtonClick(Side.SERVER, player, buttonId);
        }
    }
}
