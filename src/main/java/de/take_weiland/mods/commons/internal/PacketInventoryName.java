package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.inv.NameableInventory;
import de.take_weiland.mods.commons.inv.SCContainer;
import de.take_weiland.mods.commons.net.ModPacket;
import de.take_weiland.mods.commons.net.PacketInput;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

public class PacketInventoryName extends ModPacket {

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
	protected void handle(PacketInput in, EntityPlayer player, Side side) {
		if (player.openContainer.windowId == in.getByte() && player.openContainer instanceof SCContainer) {
			IInventory inv = ((SCContainer<?>) player.openContainer).inventory();
			if (inv instanceof NameableInventory) {
				((NameableInventory) inv).setCustomName(in.getString());
			}
		}
	}

}
