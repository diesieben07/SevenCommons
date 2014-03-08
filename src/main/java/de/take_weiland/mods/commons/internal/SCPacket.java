package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.net.ModPacket;
import de.take_weiland.mods.commons.net.SimplePacketType;

public abstract class SCPacket extends ModPacket<SCPacket.Type> {

	public static enum Type implements SimplePacketType<SCPacket.Type> {

		VIEW_UPDATES(PacketViewUpdates.class),
		UPDATE_ACTION(PacketUpdateAction.class),
		MOD_STATE(PacketModState.class),
		DOWNLOAD_PROGRESS(PacketDownloadProgress.class),
		CLIENT_ACTION(PacketClientAction.class),
		INV_NAME(PacketInventoryName.class),
		SYNC_ENTITY_PROPS_IDS(PacketEntityPropsIds.class),
		SYNC(PacketSync.class);

		private final Class<? extends ModPacket<SCPacket.Type>> packet;

		private Type(Class<? extends ModPacket<SCPacket.Type>> packet) {
			this.packet = packet;
		}

		@Override
		public Class<? extends ModPacket<Type>> packet() {
			return packet;
		}

	}

}
