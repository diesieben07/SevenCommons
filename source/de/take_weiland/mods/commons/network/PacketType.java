package de.take_weiland.mods.commons.network;

public interface PacketType {

	/**
	 * gets the channel this packet should be sent on
	 * @return the channel
	 */
	public String getChannel();
	
	/**
	 * gets this packet's id. must not exceed 255 (1 byte)
	 * @return packet id
	 */
	public byte getPacketId();
	
	public Class<? extends AbstractModPacket> getPacketClass();
	
}
