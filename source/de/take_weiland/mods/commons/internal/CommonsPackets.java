package de.take_weiland.mods.commons.internal;

import com.google.common.primitives.UnsignedBytes;

import de.take_weiland.mods.commons.network.ModPacket;
import de.take_weiland.mods.commons.network.PacketType;

public enum CommonsPackets implements PacketType {
	
	VIEW_UPDATES(PacketViewUpdates.class),
	UPDATE_ACTION(PacketUpdateAction.class),
	MOD_STATE(PacketModState.class),
	DOWNLOAD_PROGRESS(PacketDownloadProgress.class),
	TE_SYNC(PacketTileEntitySync.class);
	
	private static final String CHANNEL = "SevenCommons";
	
	private final Class<? extends ModPacket> clazz;
	
	private CommonsPackets(Class<? extends ModPacket> clazz) {
		this.clazz = clazz;
	}

	@Override
	public String getChannel() {
		return CHANNEL;
	}

	@Override
	public byte getPacketId() {
		return UnsignedBytes.checkedCast(ordinal());
	}

	@Override
	public Class<? extends ModPacket> getPacketClass() {
		return clazz;
	}

}
