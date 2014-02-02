package de.take_weiland.mods.commons.internal;

import static de.take_weiland.mods.commons.net.Packets.sendPacketToPlayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.network.packet.Packet;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.network.DataPacket;
import de.take_weiland.mods.commons.network.PacketType;
import de.take_weiland.mods.commons.templates.SyncedContainer;

public class PacketContainerSync extends DataPacket {

	private SyncedContainer<?> container;
	private boolean needsToSend;
	
	public PacketContainerSync(SyncedContainer<?> container) {
		this.container = container;
	}
	
	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeByte(((Container)container).windowId);
		needsToSend = container.writeSyncData(out);
	}
	
	@Override
	public void read(EntityPlayer player, Side side, DataInputStream in) throws IOException {
		int windowId = in.readByte();
		if (player.openContainer.windowId == windowId && player.openContainer instanceof SyncedContainer) {
			((SyncedContainer<?>) player.openContainer).readSyncData(in);
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
		return CommonsPackets.SYNC_CONTAINER;
	}
	
	public void sendToIfNeeded(EntityPlayer player) {
		Packet vanilla = make();
		if (needsToSend) {
			sendPacketToPlayer(vanilla, player);
		}
	}
}
