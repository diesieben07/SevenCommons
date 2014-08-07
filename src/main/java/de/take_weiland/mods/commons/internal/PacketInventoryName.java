package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.inv.NameableInventory;
import de.take_weiland.mods.commons.inv.SCContainer;
import de.take_weiland.mods.commons.net.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

@PacketDirection(PacketDirection.Dir.TO_CLIENT)
public class PacketInventoryName extends ModPacket {

	private int windowId;
	private String name;

	public PacketInventoryName(int windowId, String name) {
		this.windowId = windowId;
		this.name = name;
	}

	@Override
	protected void write(MCDataOutputStream out) {
		out.writeByte(windowId);
		out.writeString(name);
	}

	@Override
	protected void read(MCDataInputStream in, EntityPlayer player, Side side) {
		windowId = in.readByte();
		name = in.readString();
		if (player.openContainer.windowId == in.readByte() && player.openContainer instanceof SCContainer) {
			IInventory inv = ((SCContainer<?>) player.openContainer).inventory();
			if (inv instanceof NameableInventory) {
				((NameableInventory) inv).setCustomName(in.readString());
			}
		}
	}

	@Override
	protected void execute(EntityPlayer player, Side side) throws ProtocolException {
		if (player.openContainer.windowId == windowId && player.openContainer instanceof SCContainer) {
			IInventory inv = ((SCContainer<?>) player.openContainer).inventory();
			if (inv instanceof NameableInventory) {
				((NameableInventory) inv).setCustomName(name);
			}
		}
	}
}
