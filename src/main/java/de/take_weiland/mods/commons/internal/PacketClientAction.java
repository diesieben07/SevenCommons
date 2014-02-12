package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import de.take_weiland.mods.commons.network.DataPacket;
import de.take_weiland.mods.commons.network.PacketType;
import net.minecraft.entity.player.EntityPlayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static de.take_weiland.mods.commons.net.Packets.readEnum;
import static de.take_weiland.mods.commons.net.Packets.writeEnum;

public class PacketClientAction extends SCPacket {

	private Action action;
	
	public PacketClientAction(Action action) {
		this.action = action;
	}

	@Override
	protected void write(WritableDataBuf out) {
		writeEnum(out, action);
	}


	@Override
	protected void handle(DataBuf in, EntityPlayer player, Side side) {
		action = readEnum(in, Action.class);

		switch (action) {
			case RESTART_FAILURE:
				CommonsModContainer.proxy.displayRestartFailure();
				break;
		}
	}

	@Override
	public boolean validOn(Side side) {
		return side.isClient();
	}

	public static enum Action {
		
		RESTART_FAILURE
		
	}

}
