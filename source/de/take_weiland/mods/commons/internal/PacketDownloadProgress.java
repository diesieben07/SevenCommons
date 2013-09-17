package de.take_weiland.mods.commons.internal;

import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.relauncher.Side;
import de.take_weiland.mods.commons.internal.updater.UpdatableMod;
import de.take_weiland.mods.commons.network.PacketType;
import de.take_weiland.mods.commons.network.StreamPacket;
import de.take_weiland.mods.commons.util.MinecraftDataInput;
import de.take_weiland.mods.commons.util.MinecraftDataOutput;

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
	protected void readData(MinecraftDataInput in) {
		modId = in.readUTF();
		downloadProgress = in.readByte();
	}

	@Override
	protected void writeData(MinecraftDataOutput out) {
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
