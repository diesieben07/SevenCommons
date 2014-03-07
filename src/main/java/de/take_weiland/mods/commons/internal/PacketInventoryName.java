package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import de.take_weiland.mods.commons.templates.NameableInventory;
import de.take_weiland.mods.commons.templates.SCContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

public class PacketInventoryName extends SCPacket {

	private int windowId;
	private String name;
	
	public PacketInventoryName(int windowId, String name) {
		this.windowId = windowId;
		this.name = name;
	}

	@Override
	public boolean validOn(Side side) {
		return side.isClient();
	}

	@Override
	protected void write(WritableDataBuf out) {
		out.putByte(windowId);
		out.putString(name);
	}
	
	@Override
	protected void handle(DataBuf in, EntityPlayer player, Side side) {
		windowId = in.getByte();
		name = in.getString();

		if (player.openContainer.windowId == windowId && player.openContainer instanceof SCContainer) {
			IInventory inv = ((SCContainer<?>) player.openContainer).inventory();
			if (inv instanceof NameableInventory) {
				((NameableInventory) inv).setCustomName(name);
			}
		}
	}

}
