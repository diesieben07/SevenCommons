package de.take_weiland.mods.commons.internal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.updater.UpdatableMod;
import de.take_weiland.mods.commons.network.DataPacket;
import de.take_weiland.mods.commons.network.PacketType;

public class PacketDownloadProgress extends DataPacket {

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
	protected void read(EntityPlayer player, Side side, DataInputStream in) throws IOException {
		modId = in.readUTF();
		downloadProgress = in.readByte();
	}

	@Override
	protected void write(DataOutputStream out) throws IOException {
		out.writeUTF(modId);
		out.writeByte(downloadProgress);
	}

	@Override
	public void execute(EntityPlayer player, Side side) {
		CommonsModContainer.proxy.handleDownloadProgress(this);
	}

	@Override
	public boolean isValidForSide(Side side) {
		return side.isClient();
	}

	@Override
	public PacketType type() {
		return CommonsPackets.DOWNLOAD_PROGRESS;
	}

	public String getModId() {
		return modId;
	}

	public int getDownloadProgress() {
		return downloadProgress;
	}

}
