package de.take_weiland.mods.commons.internal;

import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.network.PacketType;
import de.take_weiland.mods.commons.network.StreamPacket;
import de.take_weiland.mods.commons.util.MinecraftDataInput;
import de.take_weiland.mods.commons.util.MinecraftDataOutput;

public class PacketClientAction extends StreamPacket {

	private Action action;
	
	public PacketClientAction(Action action) {
		this.action = action;
	}

	@Override
	protected void readData(MinecraftDataInput in) {
		action = in.readEnum(Action.class);
	}

	@Override
	protected void writeData(MinecraftDataOutput out) {
		out.writeEnum(action);
	}

	@Override
	public boolean isValidForSide(Side side) {
		return side.isClient();
	}

	@Override
	public void execute(EntityPlayer player, Side side) {
		switch (action) {
		case RESTART_FAILURE:
			CommonsModContainer.proxy.displayRestartFailure();
			break;
		}
	}

	@Override
	public PacketType type() {
		return CommonsPackets.CLIENT_ACTION;
	}
	
	public static enum Action {
		
		RESTART_FAILURE
		
	}

}
