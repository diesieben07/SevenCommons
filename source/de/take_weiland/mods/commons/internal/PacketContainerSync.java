package de.take_weiland.mods.commons.internal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.gui.AdvancedContainer;
import de.take_weiland.mods.commons.network.PacketType;
import de.take_weiland.mods.commons.network.StreamPacket;

public class PacketContainerSync extends StreamPacket {

	private AdvancedContainer<?> container;
	private int windowId;
	private boolean sendAll;
	private ByteArrayDataInput input;
	
	public <T extends Container & AdvancedContainer<?>> PacketContainerSync(T container, boolean sendAll) {
		this.container = container;
		windowId = container.windowId;
		this.sendAll = sendAll;
	}
	
	@Override
	protected void readData(ByteArrayDataInput in) {
		windowId = in.readByte();
		input = in;
	}

	@Override
	protected void writeData(ByteArrayDataOutput out) {
		out.writeByte(windowId);
		container.writeSyncData(out, sendAll);
	}

	@Override
	protected void execute(EntityPlayer player, Side side) {
		if (player.openContainer.windowId == windowId && player.openContainer instanceof AdvancedContainer) {
			((AdvancedContainer<?>)player.openContainer).readSyncData(input);
		}
	}

	@Override
	protected PacketType getType() {
		return CommonsPackets.CONTAINER_SYNC;
	}

	@Override
	protected boolean isValidForSide(Side side) {
		return side.isClient();
	}

}
