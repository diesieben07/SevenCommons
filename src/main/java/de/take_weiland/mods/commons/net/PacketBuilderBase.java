package de.take_weiland.mods.commons.net;

/**
 * @author diesieben07
 */
public interface PacketBuilderBase extends WritableDataBuf {

	/**
	 * use {@link #build()} instead
	 */
	@Deprecated
	SimplePacket toPacket();

	/**
	 * <p>Turn the contents of this PacketBuilder into a Packet. After a call to this method a PacketBuilder can no longer be used and should be discarded
	 * @return the created Packet
	 */
	SimplePacket build();

}
