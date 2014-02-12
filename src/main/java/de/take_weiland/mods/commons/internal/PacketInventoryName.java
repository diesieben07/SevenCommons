package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.network.DataPacket;
import de.take_weiland.mods.commons.network.PacketType;
import de.take_weiland.mods.commons.templates.NameableTileEntity;
import de.take_weiland.mods.commons.templates.SCContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketInventoryName extends DataPacket {

	private int windowId;
	private String name;
	
	public PacketInventoryName(int windowId, String name) {
		this.windowId = windowId;
		this.name = name;
	}

	@Override
	public boolean isValidForSide(Side side) {
		return side.isClient();
	}

	@Override
	protected void write(DataOutputStream out) throws IOException {
		out.writeByte(windowId);
		out.writeUTF(name);
	}
	
	@Override
	protected void read(EntityPlayer player, Side side, DataInputStream in) throws IOException {
		windowId = in.readByte();
		name = in.readUTF();
	}
	
	@Override
	public void execute(EntityPlayer player, Side side) {
		if (player.openContainer.windowId == windowId && player.openContainer instanceof SCContainer) {
			IInventory inv = ((SCContainer<?>) player.openContainer).inventory();
			if (inv instanceof NameableTileEntity) {
				((NameableTileEntity) inv).setCustomName(name);
			}
		}
	}
	
	@Override
	public PacketType type() {
		return CommonsPackets.INV_NAME;
	}

}
