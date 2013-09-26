package de.take_weiland.mods.commons.internal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.gui.AdvancedContainer;
import de.take_weiland.mods.commons.network.DataPacket;
import de.take_weiland.mods.commons.network.PacketType;

public class PacketSync extends DataPacket {

	private AdvancedContainer<?> container;
	
	public PacketSync(AdvancedContainer<?> container) {
		this.container = container;
	}
	
	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeByte(((Container)container).windowId);
		container.writeSyncData(out);
	}
	
	@Override
	public void read(EntityPlayer player, Side side, DataInputStream in) throws IOException {
		int windowId = in.readByte();
		if (player.openContainer.windowId == windowId && player.openContainer instanceof AdvancedContainer) {
			((AdvancedContainer<?>) player.openContainer).readSyncData(in);
		}
	}

	@Override
	public void execute(EntityPlayer player, Side side) { }

	@Override
	public boolean isValidForSide(Side side) {
		return side.isClient();
	}

	@Override
	public PacketType type() {
		return CommonsPackets.SYNC;
	}
}
