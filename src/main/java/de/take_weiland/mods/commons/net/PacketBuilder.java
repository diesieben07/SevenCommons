package de.take_weiland.mods.commons.net;

/**
 * <p>A PacketBuilder is like a {@link de.take_weiland.mods.commons.net.WritableDataBuf}, but it can be used to send a Packet</p>
 * <p>Obtain one with a {@link de.take_weiland.mods.commons.net.PacketFactory}</p>
 */
public interface PacketBuilder extends WritableDataBuf {

	@Override
	PacketBuilder writeBoolean(boolean b);

	@Override
	PacketBuilder writeByte(int b);

	@Override
	PacketBuilder writeShort(int s);

	@Override
	PacketBuilder writeInt(int i);

	@Override
	PacketBuilder writeLong(long l);

	@Override
	PacketBuilder writeChar(char c);

	@Override
	PacketBuilder writeFloat(float f);

	@Override
	PacketBuilder writeDouble(double d);

	@Override
	PacketBuilder writeString(String s);

	@Override
	PacketBuilder writeVarInt(int i);

	@Override
	PacketBuilder writeUnsignedByte(int i);

	@Override
	PacketBuilder writeUnsignedShort(int i);

	@Override
	PacketBuilder writeRaw(byte[] bytes);

	@Override
	PacketBuilder writeRaw(byte[] bytes, int off, int len);

	@Override
	PacketBuilder grow(int n);

	/**
	 * use {@link #build()} instead
	 */
	@Deprecated
	SimplePacket toPacket();

	/**
	 * <p>Turn the contents of this PacketBuilder into a Packet. After a call to this method a PacketBuilder can no longer be used and should be discarded
	 *
	 * @return the created Packet
	 */
	SimplePacket build();
}
