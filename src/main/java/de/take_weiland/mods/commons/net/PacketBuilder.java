package de.take_weiland.mods.commons.net;

public interface PacketBuilder extends WritableDataBuf {

	SimplePacket toPacket();
	
	PacketBuilder putBoolean(boolean b);
	PacketBuilder putByte(byte b);
	PacketBuilder putShort(short s);
	PacketBuilder putInt(int i);
	PacketBuilder putLong(long l);
	PacketBuilder putChar(char c);
	PacketBuilder putFloat(float f);
	PacketBuilder putDouble(double d);
	
	PacketBuilder putVarInt(int i);
	PacketBuilder putUnsignedByte(int i);
	PacketBuilder putUnsignedShort(int i);
	
	PacketBuilder put(byte[] bytes);
	PacketBuilder put(byte[] bytes, int off, int len);
	
	PacketBuilder grow(int n);
	
}
