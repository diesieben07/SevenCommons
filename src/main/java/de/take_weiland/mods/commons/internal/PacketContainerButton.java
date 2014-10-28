package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.inv.ButtonContainer;
import de.take_weiland.mods.commons.net.*;
import net.minecraft.entity.player.EntityPlayer;

import java.io.IOException;

/**
 * @author diesieben07
 */
@PacketDirection(PacketDirection.Dir.TO_SERVER)
public final class PacketContainerButton extends ModPacket {

	private int windowId;
	private int buttonId;

	public PacketContainerButton(int windowId, int buttonId) {
		this.windowId = windowId;
		this.buttonId = buttonId;
	}

	@Override
	protected void write(MCDataOutputStream out) {
		out.writeByte(windowId);
		out.writeVarInt(buttonId);
	}

	@Override
	protected void read(MCDataInputStream in, EntityPlayer player, Side side) throws IOException, ProtocolException {
		windowId = in.readUnsignedByte();
		buttonId = in.readVarInt();
	}

	@Override
	protected void execute(EntityPlayer player, Side side) throws ProtocolException {
		if (player.openContainer.windowId == windowId && player.openContainer instanceof ButtonContainer) {
			((ButtonContainer) player.openContainer).onButtonClick(Side.SERVER, player, buttonId);
		}
	}
}
