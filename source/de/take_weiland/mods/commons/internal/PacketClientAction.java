package de.take_weiland.mods.commons.internal;

import net.minecraft.entity.player.EntityPlayer;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.network.PacketType;
import de.take_weiland.mods.commons.network.StreamPacket;

public class PacketClientAction extends StreamPacket {

	private Action action;
	
	public PacketClientAction(Action action) {
		this.action = action;
	}

	@Override
	protected void readData(ByteArrayDataInput in) {
		action = readEnum(Action.class, in);
	}

	@Override
	protected void writeData(ByteArrayDataOutput out) {
		writeEnum(action, out);
	}

	@Override
	protected boolean isValidForSide(Side side) {
		return side.isClient();
	}

	@Override
	protected void execute(EntityPlayer player, Side side) {
		switch (action) {
		case RESTART_FAILURE:
			CommonsModContainer.proxy.displayRestartFailure();
			break;
		}
	}

	@Override
	protected PacketType getType() {
		return CommonsPackets.CLIENT_ACTION;
	}
	
	public static enum Action {
		
		RESTART_FAILURE
		
	}

}
