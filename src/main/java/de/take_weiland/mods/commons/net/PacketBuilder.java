package de.take_weiland.mods.commons.net;

/**
 * <p>A PacketBuilder is like a {@link de.take_weiland.mods.commons.net.WritableDataBuf}, but it can be used to send a Packet</p>
 * <p>Obtain one with a {@link de.take_weiland.mods.commons.net.PacketFactory}</p>
 */
public interface PacketBuilder extends PacketBuilderBase {

	@Override
	PacketBuilder putBoolean(boolean b);

	@Override
	PacketBuilder putByte(int b);

	@Override
	PacketBuilder putShort(int s);

	@Override
	PacketBuilder putInt(int i);

	@Override
	PacketBuilder putLong(long l);

	@Override
	PacketBuilder putChar(char c);

	@Override
	PacketBuilder putFloat(float f);

	@Override
	PacketBuilder putDouble(double d);

	@Override
	PacketBuilder putString(String s);

	@Override
	PacketBuilder putVarInt(int i);

	@Override
	PacketBuilder putUnsignedByte(int i);

	@Override
	PacketBuilder putUnsignedShort(int i);

	@Override
	PacketBuilder put(byte[] bytes);

	@Override
	PacketBuilder putRaw(byte[] bytes);

	@Override
	PacketBuilder putRaw(byte[] bytes, int off, int len);

	@Override
	PacketBuilder grow(int n);
}
