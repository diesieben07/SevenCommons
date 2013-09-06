package de.take_weiland.mods.commons.internal;

import com.google.common.primitives.UnsignedBytes;

import de.take_weiland.mods.commons.network.AbstractModPacket;
import de.take_weiland.mods.commons.network.PacketType;
import de.take_weiland.mods.commons.syncing.PacketSync;

public enum CommonsPackets implements PacketType {
	
	VIEW_UPDATES(PacketViewUpdates.class),
	UPDATE_ACTION(PacketUpdateAction.class),
	MOD_STATE(PacketModState.class),
	DOWNLOAD_PROGRESS(PacketDownloadProgress.class),
	CLIENT_ACTION(PacketClientAction.class),
	SYNC(PacketSync.class);
	
	private static final String CHANNEL = "SevenCommons";
	
	private final Class<? extends AbstractModPacket> clazz;
	
	private CommonsPackets(Class<? extends AbstractModPacket> clazz) {
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
	public Class<? extends AbstractModPacket> getPacketClass() {
		return clazz;
	}

}
