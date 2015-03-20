package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.inv.Containers;
import de.take_weiland.mods.commons.inv.NameableInventory;
import de.take_weiland.mods.commons.net.*;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

@PacketDirection(PacketDirection.Dir.TO_CLIENT)
public class PacketInventoryName extends ModPacket {

	private int windowId;
	private int invIdx;
	private String name;

	public PacketInventoryName(int windowId, int invIdx, String name) {
		this.windowId = windowId;
		this.invIdx = invIdx;
		this.name = name;
	}

	@Override
    public void write(MCDataOutputStream out) {
		out.writeByte(windowId);
		out.writeByte(invIdx);
		out.writeString(name);
	}

	@Override
    public void read(MCDataInputStream in, EntityPlayer player, Side side) {
		windowId = in.readByte();
		invIdx = in.readByte();
		name = in.readString();
	}

	@Override
    public void execute(EntityPlayer player, Side side) throws ProtocolException {
		if (player.openContainer.windowId == windowId) {
			IInventory inv = JavaUtils.get(Containers.getInventories(player.openContainer).asList(), invIdx);
			if (inv instanceof NameableInventory) {
				((NameableInventory) inv).setCustomName(name);
			}
		}
	}
}
