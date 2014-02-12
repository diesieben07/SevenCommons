package de.take_weiland.mods.commons.internal;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.updater.UpdatableMod;
import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.WritableDataBuf;
import de.take_weiland.mods.commons.network.DataPacket;
import de.take_weiland.mods.commons.network.PacketType;
import net.minecraft.entity.player.EntityPlayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketDownloadProgress extends SCPacket {

	private String modId;
	private int downloadProgress;
	
	/**
	 * 
	 * @param mod
	 * @param downloadProgress in percent, -1 for no progress
	 */
	public PacketDownloadProgress(UpdatableMod mod, int downloadProgress) {
		this.modId = mod.getModId();
		this.downloadProgress = downloadProgress;
	}

	@Override
	protected void write(WritableDataBuf out) {
		out.putString(modId);
		out.putByte(downloadProgress);
	}

	@Override
	protected void handle(DataBuf in, EntityPlayer player, Side side) {
		modId = in.getString();
		downloadProgress = in.getByte();

		CommonsModContainer.proxy.handleDownloadProgress(this);
	}

	@Override
	public boolean validOn(Side side) {
		return side.isClient();
	}

	public String getModId() {
		return modId;
	}

	public int getDownloadProgress() {
		return downloadProgress;
	}

}
