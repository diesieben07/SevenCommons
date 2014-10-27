package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.inv.NameableInventory;
import de.take_weiland.mods.commons.net.*;
import de.take_weiland.mods.commons.util.JavaUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

@PacketDirection(PacketDirection.Dir.TO_CLIENT)
public class PacketInventoryName extends ModPacket {

	private int windowId;
	private int slotIdx;
	private String name;

	public PacketInventoryName(int windowId, int slotIdx, String name) {
		this.windowId = windowId;
		this.slotIdx = slotIdx;
		this.name = name;
	}

	@Override
	protected void write(MCDataOutputStream out) {
		out.writeByte(windowId);
		out.writeVarInt(slotIdx);
		out.writeString(name);
	}

	@Override
	protected void read(MCDataInputStream in, EntityPlayer player, Side side) {
		windowId = in.readByte();
		slotIdx = in.readVarInt();
		name = in.readString();
	}

	@Override
	protected void execute(EntityPlayer player, Side side) throws ProtocolException {
		if (player.openContainer.windowId == windowId) {
			Slot slot = (Slot) JavaUtils.get(player.openContainer.inventorySlots, slotIdx);
			if (slot != null && slot.inventory instanceof NameableInventory) {
				((NameableInventory) slot.inventory).setCustomName(name);
			}
		}
	}
}
