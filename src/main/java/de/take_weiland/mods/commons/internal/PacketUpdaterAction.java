package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.exclude.SCModContainer;
import de.take_weiland.mods.commons.internal.updater.UpdateController;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.DataBuffers;
import de.take_weiland.mods.commons.net.ModPacket;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author diesieben07
 */
public class PacketUpdaterAction extends ModPacket {

	private Action action;

	public PacketUpdaterAction(Action action) {
		this.action = action;
	}

	@Override
	protected boolean validOn(Side side) {
		return side.isServer();
	}

	@Override
	protected void write(WritableDataBuf buffer) {
		DataBuffers.writeEnum(buffer, action);
	}

	@Override
	protected void handle(DataBuf buffer, EntityPlayer player, Side side) {
		action = DataBuffers.readEnum(buffer, Action.class);
		if (SCModContainer.updaterEnabled && player == ServerProxy.currentUpdateViewer) {
			UpdateController uc = SCModContainer.updateController;
			switch (action) {
				case REFRESH:
					uc.searchForUpdates();
					break;
				case OPTIMIZE:
					uc.optimizeVersionSelection();
					break;
				case INSTALL:
					uc.performInstall();
					break;
				case RESET:
					uc.resetFailure();
					break;
				case RESTART:
					uc.restartMinecraft();
					break;
				case CLOSE:
					ServerProxy.resetUpdateViewer(player);
					break;
			}
		}
	}

	public static enum Action {
		REFRESH,
		OPTIMIZE,
		INSTALL,
		RESET,
		RESTART,
		CLOSE
	}
}
