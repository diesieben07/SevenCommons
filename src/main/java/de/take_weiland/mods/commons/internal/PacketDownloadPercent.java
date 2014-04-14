package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.exclude.SCModContainer;
import de.take_weiland.mods.commons.net.ModPacket;
import de.take_weiland.mods.commons.net.PacketInput;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author diesieben07
 */
public class PacketDownloadPercent extends ModPacket {

	private int percent;

	public PacketDownloadPercent(int percent) {
		this.percent = percent;
	}

	@Override
	protected boolean validOn(Side side) {
		return side.isClient();
	}

	@Override
	protected void write(WritableDataBuf buffer) {
		buffer.putByte(percent);
	}

	@Override
	protected void handle(PacketInput buffer, EntityPlayer player, Side side) {
		percent = buffer.getByte();
		SCModContainer.proxy.handleDownloadPercent(percent);
	}
}
