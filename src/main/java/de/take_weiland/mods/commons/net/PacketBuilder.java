package de.take_weiland.mods.commons.net;

/**
 * <p>A PacketBuilder is like a {@link de.take_weiland.mods.commons.net.WritableDataBuf}, but it can be used to send a Packet</p>
 * <p>Obtain one with a {@link de.take_weiland.mods.commons.net.PacketFactory}</p>
 */
public interface PacketBuilder extends PacketBuilderBase {

	PacketBuilder.WithResponse onResponse(PacketResponseHandler handler);

	interface WithResponse extends PacketBuilderBase {

		@Override
		WithResponse putBoolean(boolean b);

		@Override
		WithResponse putByte(int b);

		@Override
		WithResponse putShort(int s);

		@Override
		WithResponse putInt(int i);

		@Override
		WithResponse putLong(long l);

		@Override
		WithResponse putChar(char c);

		@Override
		WithResponse putFloat(float f);

		@Override
		WithResponse putDouble(double d);

		@Override
		WithResponse putString(String s);

		@Override
		WithResponse putVarInt(int i);

		@Override
		WithResponse putUnsignedByte(int i);

		@Override
		WithResponse putUnsignedShort(int i);

		@Override
		WithResponse putBytes(byte[] bytes);

		@Override
		WithResponse putRaw(byte[] bytes);

		@Override
		WithResponse putRaw(byte[] bytes, int off, int len);

		@Override
		WithResponse grow(int n);
	}

	interface ForResponse extends WritableDataBuf {

		void send();

		@Override
		ForResponse putBoolean(boolean b);

		@Override
		ForResponse putByte(int b);

		@Override
		ForResponse putShort(int s);

		@Override
		ForResponse putInt(int i);

		@Override
		ForResponse putLong(long l);

		@Override
		ForResponse putChar(char c);

		@Override
		ForResponse putFloat(float f);

		@Override
		ForResponse putDouble(double d);

		@Override
		ForResponse putString(String s);

		@Override
		ForResponse putVarInt(int i);

		@Override
		ForResponse putUnsignedByte(int i);

		@Override
		ForResponse putUnsignedShort(int i);

		@Override
		ForResponse putBytes(byte[] bytes);

		@Override
		ForResponse putRaw(byte[] bytes);

		@Override
		ForResponse putRaw(byte[] bytes, int off, int len);

		@Override
		ForResponse grow(int n);
	}

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
	PacketBuilder putBytes(byte[] bytes);

	@Override
	PacketBuilder putRaw(byte[] bytes);

	@Override
	PacketBuilder putRaw(byte[] bytes, int off, int len);

	@Override
	PacketBuilder grow(int n);
}
