package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.DataBuf;
import de.take_weiland.mods.commons.net.PacketBuilder;

/**
 * @author diesieben07
 */
@SuppressWarnings("unchecked")
final class PrimitiveWrapperAdapter {

	static class OfBoolean extends ImmutableAdapter<Boolean> {

		@Override
		public void write(Boolean value, PacketBuilder builder) {
			builder.writeBoolean(value);
		}

		@Override
		public <ACTUAL_T extends Boolean> ACTUAL_T read(ACTUAL_T prevVal, DataBuf buf) {
			return (ACTUAL_T) Boolean.valueOf(buf.readBoolean());
		}

	}

	static class OfByte extends ImmutableAdapter<Byte> {

		@Override
		public void write(Byte value, PacketBuilder builder) {
			builder.writeByte(value);
		}

		@Override
		public <ACTUAL_T extends Byte> ACTUAL_T read(ACTUAL_T prevVal, DataBuf buf) {
			return (ACTUAL_T) Byte.valueOf(buf.readByte());
		}
	}

	static class OfShort extends ImmutableAdapter<Short> {

		@Override
		public void write(Short value, PacketBuilder builder) {
			builder.writeShort(value);
		}

		@Override
		public <ACTUAL_T extends Short> ACTUAL_T read(ACTUAL_T prevVal, DataBuf buf) {
			return (ACTUAL_T) Short.valueOf(buf.readShort());
		}
	}

	static class OfInt extends ImmutableAdapter<Integer> {

		@Override
		public void write(Integer value, PacketBuilder builder) {
			builder.writeInt(value);
		}

		@Override
		public <ACTUAL_T extends Integer> ACTUAL_T read(ACTUAL_T prevVal, DataBuf buf) {
			return (ACTUAL_T) Integer.valueOf(buf.readInt());
		}
	}

	static class OfLong extends ImmutableAdapter<Long> {

		@Override
		public void write(Long value, PacketBuilder builder) {
			builder.writeLong(value);
		}

		@Override
		public <ACTUAL_T extends Long> ACTUAL_T read(ACTUAL_T prevVal, DataBuf buf) {
			return (ACTUAL_T) Long.valueOf(buf.readLong());
		}
	}

	static class OfFloat extends ImmutableAdapter<Float> {

		@Override
		public void write(Float value, PacketBuilder builder) {
			builder.writeFloat(value);
		}

		@Override
		public <ACTUAL_T extends Float> ACTUAL_T read(ACTUAL_T prevVal, DataBuf buf) {
			return (ACTUAL_T) Float.valueOf(buf.readFloat());
		}
	}

	static class OfDouble extends ImmutableAdapter<Double> {

		@Override
		public void write(Double value, PacketBuilder builder) {
			builder.writeDouble(value);
		}

		@Override
		public <ACTUAL_T extends Double> ACTUAL_T read(ACTUAL_T prevVal, DataBuf buf) {
			return (ACTUAL_T) Double.valueOf(buf.readDouble());
		}
	}

	static class OfChar extends ImmutableAdapter<Character> {

		@Override
		public void write(Character value, PacketBuilder builder) {
			builder.writeChar(value);
		}

		@Override
		public <ACTUAL_T extends Character> ACTUAL_T read(ACTUAL_T prevVal, DataBuf buf) {
			return (ACTUAL_T) Character.valueOf(buf.readChar());
		}
	}

	static class OfBooleanCreator extends AdapterCreator<Boolean> {

		@Override
		public SyncAdapter<Boolean> newInstance() {
			return new OfBoolean();
		}
	}

	static class OfByteCreator extends AdapterCreator<Byte> {

		@Override
		public SyncAdapter<Byte> newInstance() {
			return new OfByte();
		}
	}

	static class OfShortCreator extends AdapterCreator<Short> {

		@Override
		public SyncAdapter<Short> newInstance() {
			return new OfShort();
		}
	}

	static class OfIntCreator extends AdapterCreator<Integer> {

		@Override
		public SyncAdapter<Integer> newInstance() {
			return new OfInt();
		}
	}

	static class OfLongCreator extends AdapterCreator<Long> {

		@Override
		public SyncAdapter<Long> newInstance() {
			return new OfLong();
		}
	}

	static class OfFloatCreator extends AdapterCreator<Float> {

		@Override
		public SyncAdapter<Float> newInstance() {
			return new OfFloat();
		}
	}

	static class OfDoubleCreator extends AdapterCreator<Double> {

		@Override
		public SyncAdapter<Double> newInstance() {
			return new OfDouble();
		}
	}

	static class OfCharCreator extends AdapterCreator<Character> {

		@Override
		public SyncAdapter<Character> newInstance() {
			return new OfChar();
		}
	}
}
