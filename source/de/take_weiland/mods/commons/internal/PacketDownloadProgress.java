package de.take_weiland.mods.commons.internal;

import net.minecraft.entity.player.EntityPlayer;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.updater.UpdatableMod;
import de.take_weiland.mods.commons.network.StreamPacket;
import de.take_weiland.mods.commons.network.PacketType;

public class PacketDownloadProgress extends StreamPacket {

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
	protected void readData(ByteArrayDataInput in) {
		modId = in.readUTF();
		downloadProgress = in.readByte();
	}

	@Override
	protected void writeData(ByteArrayDataOutput out) {
		out.writeUTF(modId);
		out.writeByte(downloadProgress);
	}

	@Override
	protected void execute(EntityPlayer player, Side side) {
		CommonsModContainer.proxy.handleDownloadProgress(this);
	}

	@Override
	protected boolean isValidForSide(Side side) {
		return side.isClient();
	}

	@Override
	protected PacketType getType() {
		return CommonsPackets.DOWNLOAD_PROGRESS;
	}

	public String getModId() {
		return modId;
	}

	public int getDownloadProgress() {
		return downloadProgress;
	}

}
