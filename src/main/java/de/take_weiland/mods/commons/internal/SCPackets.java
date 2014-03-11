package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.net.ModPacket;
import de.take_weiland.mods.commons.net.SimplePacketType;

/**
* @author diesieben07
*/
public enum SCPackets implements SimplePacketType {

	VIEW_UPDATES(PacketViewUpdates.class),
	UPDATE_ACTION(PacketUpdateAction.class),
	MOD_STATE(PacketModState.class),
	DOWNLOAD_PROGRESS(PacketDownloadProgress.class),
	CLIENT_ACTION(PacketClientAction.class),
	INV_NAME(PacketInventoryName.class),
	SYNC_ENTITY_PROPS_IDS(PacketEntityPropsIds.class),
	SYNC(PacketSync.class);

	private final Class<? extends ModPacket> packet;

	SCPackets(Class<? extends ModPacket> packet) {
		this.packet = packet;
	}

	@Override
	public Class<? extends ModPacket> packet() {
		return packet;
	}

}
