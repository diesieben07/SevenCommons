package de.take_weiland.mods.commons.internal;

import de.take_weiland.mods.commons.net.ModPacket;
import de.take_weiland.mods.commons.net.SimplePacket;
import de.take_weiland.mods.commons.net.SimplePacketType;
import de.take_weiland.mods.commons.sync.PacketSync;

public abstract class SCPacket extends ModPacket<SCPacket.Type> {

	public static enum Type implements SimplePacketType<SCPacket.Type> {

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
