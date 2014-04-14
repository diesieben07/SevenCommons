package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.exclude.SCModContainer;
import de.take_weiland.mods.commons.internal.updater.UpdateControllerRemote;
import de.take_weiland.mods.commons.net.ModPacket;
import de.take_weiland.mods.commons.net.PacketInput;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author diesieben07
 */
public class PacketDisplayUpdates extends ModPacket {

	@Override
	protected void write(WritableDataBuf buffer) {
		SCModContainer.updateController.writeMods(buffer);
	}

	@Override
	protected void handle(PacketInput buffer, EntityPlayer player, Side side) {
		UpdateControllerRemote controller = new UpdateControllerRemote();
		controller.readMods(buffer);
		SCModContainer.proxy.displayUpdateGui(controller);
	}

	@Override
	protected boolean validOn(Side side) {
		return side.isClient();
	}
}
