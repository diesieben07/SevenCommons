package de.take_weiland.mods.commons.net;

/**
 * <p>A PacketBuilder is like a {@link de.take_weiland.mods.commons.net.WritableDataBuf}, but it can be used to send a Packet</p>
 * <p>Obtain one with a {@link de.take_weiland.mods.commons.net.PacketFactory}</p>
 */
public interface PacketBuilder extends WritableDataBuf {

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
	
	PacketBuilder putBoolean(boolean b);
	PacketBuilder putByte(int b);
	PacketBuilder putShort(int s);
	PacketBuilder putInt(int i);
	PacketBuilder putLong(long l);
	PacketBuilder putChar(char c);
	PacketBuilder putFloat(float f);
	PacketBuilder putDouble(double d);
	PacketBuilder putString(String s);
	
	PacketBuilder putVarInt(int i);
	PacketBuilder putUnsignedByte(int i);
	PacketBuilder putUnsignedShort(int i);
	
	PacketBuilder putBytes(byte[] bytes);
	
	PacketBuilder putRaw(byte[] bytes);
	PacketBuilder putRaw(byte[] bytes, int off, int len);
	
	PacketBuilder grow(int n);
	
}
