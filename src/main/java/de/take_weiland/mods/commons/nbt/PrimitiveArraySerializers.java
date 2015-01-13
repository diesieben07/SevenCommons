package de.take_weiland.mods.commons.nbt;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;

import javax.annotation.Nullable;

import static de.take_weiland.mods.commons.nbt.NBTData.isSerializedNull;
import static de.take_weiland.mods.commons.nbt.NBTData.serializedNull;

/**
 * @author diesieben07
 */
class PrimitiveArraySerializers {

	static final class Byte implements NBTSerializer.NullSafe<byte[]> {

		@Override
		public NBTBase serialize(byte[] instance) {
			return instance == null ? NBT.serializedNull() : new NBTTagByteArray("", instance);
		}

		@Override
		public byte[] deserialize(NBTBase nbt) {
			return NBT.isSerializedNull(nbt) ? null : ((NBTTagByteArray) nbt).byteArray;
		}
	}

	static final class Short implements NBTSerializer.NullSafe<short[]> {

		@Override
			public NBTBase serialize(@Nullable short[] instance) {
			return instance == null ? serializedNull() : new NBTTagByteArray("", ArrayConversions.encodeShorts(instance));
		}

		@Override
		public short[] deserialize(@Nullable NBTBase nbt) {
			return isSerializedNull(nbt) ? null : ArrayConversions.decodeShorts(((NBTTagByteArray) nbt).byteArray);
		}
	}

	static final class Int implements NBTSerializer.NullSafe<int[]> {

		@Override
		public NBTBase serialize(@Nullable int[] instance) {
			return instance == null ? serializedNull() : new NBTTagIntArray("", instance);
		}

		@Override
		public int[] deserialize(@Nullable NBTBase nbt) {
			return isSerializedNull(nbt) ? null : ((NBTTagIntArray) nbt).intArray;
		}
	}

	static final class Char implements NBTSerializer.NullSafe<char[]> {

		@Override
		public NBTBase serialize(@Nullable char[] instance) {
			return instance == null ? serializedNull() : new NBTTagByteArray("", ArrayConversions.encodeChars(instance));
		}

		@Override
		public char[] deserialize(@Nullable NBTBase nbt) {
			return isSerializedNull(nbt) ? null : ArrayConversions.decodeChars(((NBTTagByteArray) nbt).byteArray);
		}
	}

	static final class Long implements NBTSerializer.NullSafe<long[]> {

		@Override
		public NBTBase serialize(@Nullable long[] instance) {
			return instance == null ? serializedNull() : new NBTTagIntArray("", ArrayConversions.encodeLongs(instance));
		}

		@Override
		public long[] deserialize(@Nullable NBTBase nbt) {
			return isSerializedNull(nbt) ? null : ArrayConversions.decodeLongs(((NBTTagIntArray) nbt).intArray);
		}
	}

	static final class Float implements NBTSerializer.NullSafe<float[]> {

		@Override
		public NBTBase serialize(@Nullable float[] instance) {
			return instance == null ? serializedNull() : new NBTTagIntArray("", ArrayConversions.encodeFloats(instance));
		}

		@Override
		public float[] deserialize(@Nullable NBTBase nbt) {
			return isSerializedNull(nbt) ? null : ArrayConversions.decodeFloats(((NBTTagIntArray) nbt).intArray);
		}
	}

	static final class Double implements NBTSerializer.NullSafe<double[]> {

		@Override
		public NBTBase serialize(@Nullable double[] instance) {
			return instance == null ? serializedNull() : new NBTTagIntArray("", ArrayConversions.encodeDoubles(instance));
		}

		@Override
		public double[] deserialize(@Nullable NBTBase nbt) {
			return isSerializedNull(nbt) ? null : ArrayConversions.decodeDoubles(((NBTTagIntArray) nbt).intArray);
		}
	}

	static final class Boolean implements NBTSerializer.NullSafe<boolean[]> {

		private static final String KEY_ARR = "a";
		private static final String KEY_SIZE = "s";

		@Override
		public NBTBase serialize(@Nullable boolean[] instance) {
			if (instance == null) {
				return serializedNull();
			} else {
				NBTTagCompound nbt  = new NBTTagCompound();
				nbt.setInteger(KEY_SIZE, instance.length);
				nbt.setByteArray(KEY_SIZE, ArrayConversions.encodeBooleans(instance));
				return nbt;
			}
		}

		@Override
		public boolean[] deserialize(@Nullable NBTBase nbt) {
			if (isSerializedNull(nbt)) {
				return null;
			} else {
				NBTTagCompound comp = (NBTTagCompound) nbt;
				return ArrayConversions.decodeBooleans(comp.getByteArray(KEY_ARR), comp.getInteger(KEY_SIZE));
			}
		}
	}

}
