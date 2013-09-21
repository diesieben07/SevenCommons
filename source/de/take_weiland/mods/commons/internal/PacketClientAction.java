package de.take_weiland.mods.commons.internal;

import static de.take_weiland.mods.commons.network.Packets.readEnum;
import static de.take_weiland.mods.commons.network.Packets.writeEnum;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.network.DataPacket;
import de.take_weiland.mods.commons.network.PacketType;

public class PacketClientAction extends DataPacket {

	private Action action;
	
	public PacketClientAction(Action action) {
		this.action = action;
	}

	@Override
	protected void read(EntityPlayer player, DataInputStream in) throws IOException {
		action = readEnum(in, Action.class);
	}

	@Override
	protected void write(DataOutputStream out) throws IOException {
		writeEnum(out, action);
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
