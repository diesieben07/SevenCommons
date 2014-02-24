package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.PacketTarget;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import de.take_weiland.mods.commons.templates.SyncedContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.network.packet.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketContainerSync extends SCPacket {

	private SyncedContainer<?> container;
	private boolean needsToSend;
	
	public PacketContainerSync(SyncedContainer<?> container) {
		this.container = container;
	}
	
	@Override
	public void write(WritableDataBuf out) {
		out.putByte(((Container)container).windowId);
		try {
			needsToSend = container.writeSyncData((DataOutputStream) out.asDataOutput()); // implementation detail, yes it is a DataOutputStream
		} catch (IOException e) {
			throw new AssertionError("Impossible");
		}
	}
	
	@Override
	public void handle(DataBuf in, EntityPlayer player, Side side) {
		int windowId = in.getByte();
		if (player.openContainer.windowId == windowId && player.openContainer instanceof SyncedContainer) {
			try {
				((SyncedContainer<?>) player.openContainer).readSyncData((DataInputStream) in.asDataInput()); // see above
			} catch (IOException e) {
				throw new AssertionError("Impossible");
			}
		}
	}

	@Override
	public boolean validOn(Side side) {
		return side.isClient();
	}

	private static final PacketTarget DUMMY = new PacketTarget() {
		@Override
		public void send(Packet packet) { }
	};

	public void sendToIfNeeded(EntityPlayer player) {
		sendTo(DUMMY); // dirty hack, force write() to be called
		if (needsToSend) {
			sendTo(player);
		}
	}
}
