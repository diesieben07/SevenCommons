package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.network.ModPacket;
import de.take_weiland.mods.commons.network.PacketTransport;
import de.take_weiland.mods.commons.network.PacketType;
import de.take_weiland.mods.commons.syncing.PacketSync;

public enum CommonsPackets implements PacketType {
	
	VIEW_UPDATES(PacketViewUpdates.class),
	UPDATE_ACTION(PacketUpdateAction.class),
	MOD_STATE(PacketModState.class),
	DOWNLOAD_PROGRESS(PacketDownloadProgress.class),
	CLIENT_ACTION(PacketClientAction.class),
	SYNC(PacketSync.class);
	
	private final Class<? extends ModPacket> clazz;
	
	private CommonsPackets(Class<? extends ModPacket> clazz) {
		this.clazz = clazz;
	}

	@Override
	public int packetId() {
		return ordinal();
	}

	@Override
	public Class<? extends ModPacket> packetClass() {
		return clazz;
	}

	@Override
	public PacketTransport transport() {
		return CommonsModContainer.packetTransport;
	}

}
