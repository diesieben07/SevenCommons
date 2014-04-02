package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.exclude.SCModContainer;
import de.take_weiland.mods.commons.net.ModPacket;
import de.take_weiland.mods.commons.net.PacketInput;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import net.minecraft.entity.player.EntityPlayer;

import static de.take_weiland.mods.commons.net.DataBuffers.readEnum;
import static de.take_weiland.mods.commons.net.DataBuffers.writeEnum;

public class PacketClientAction extends ModPacket {

	private Action action;
	
	public PacketClientAction(Action action) {
		this.action = action;
	}

	@Override
	protected void write(WritableDataBuf out) {
		writeEnum(out, action);
	}


	@Override
	protected void handle(PacketInput in, EntityPlayer player, Side side) {
		action = readEnum(in, Action.class);

		switch (action) {
			case RESTART_FAILURE:
				SCModContainer.proxy.displayRestartFailure();
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
