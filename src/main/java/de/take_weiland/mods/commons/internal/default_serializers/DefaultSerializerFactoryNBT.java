package de.take_weiland.mods.commons.internal.default_serializers;

import com.google.common.collect.Sets;
import com.google.common.primitives.Primitives;
import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.reflect.PropertyAccess;
import de.take_weiland.mods.commons.serialize.bytes.ByteStreamSerializer;
import de.take_weiland.mods.commons.serialize.nbt.NBTSerializer;
import de.take_weiland.mods.commons.serialize.SerializationException;
import de.take_weiland.mods.commons.serialize.SerializationMethod;
import de.take_weiland.mods.commons.serialize.SerializerRegistry;
import net.minecraft.nbt.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.IntFunction;

import static de.take_weiland.mods.commons.serialize.SerializationMethod.VALUE;

/**
 * @author diesieben07
 */
public final class DefaultSerializerFactoryNBT implements NBTSerializer.Factory {


    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> NBTSerializer<T> getSerializer(@Nullable SerializationMethod method, @Nonnull TypeToken<T> type, @Nonnull SerializerRegistry registry) {
        Class<?> raw = Primitives.unwrap(type.getRawType());

        if (method == VALUE || method == null) {
            if (raw == boolean.class) {
                return (NBTSerializer<T>) Z.I;
            } else if (raw == byte.class) {
                return (NBTSerializer<T>) B.I;
            } else if (raw == short.class) {
                return (NBTSerializer<T>) S.I;
            } else if (raw == char.class) {
                return (NBTSerializer<T>) C.I;
            } else if (raw == int.class) {
                return (NBTSerializer<T>) I.I;
            } else if (raw == long.class) {
                return (NBTSerializer<T>) L.I;
            } else if (raw == float.class) {
                return (NBTSerializer<T>) F.I;
            } else if (raw == double.class) {
                return (NBTSerializer<T>) D.I;
            } else if (raw == boolean[].class) {
                return (NBTSerializer<T>) ZA.I;
            } else if (raw == byte[].class) {
                return (NBTSerializer<T>) BA.I;
            } else if (raw == short[].class) {
                return (NBTSerializer<T>) SA.I;
            } else if (raw == char[].class) {
                return (NBTSerializer<T>) CA.I;
            } else if (raw == int[].class) {
                return (NBTSerializer<T>) IA.I;
            } else if (raw == long[].class) {
                return (NBTSerializer<T>) LA.I;
            } else if (raw == float[].class) {
                return (NBTSerializer<T>) FA.I;
            } else if (raw == double[].class) {
                return (NBTSerializer<T>) DA.I;
            } else if (raw == String.class) {
                return (NBTSerializer<T>) String_.I;
            } else if (raw == UUID.class) {
                return (NBTSerializer<T>) UUID_.I;
            } else if (raw == BigInteger.class) {
                return (NBTSerializer<T>) BigInteger_.I;
            } else if (raw == BigDecimal.class) {
                return (NBTSerializer<T>) BigDecimal_.I;
            } else if (raw == URL.class) {
                return (NBTSerializer<T>) URL_.I;
            } else if (raw == URI.class) {
                return (NBTSerializer<T>) URI_.I;
            } else if (raw == BitSet.class) {
                return (NBTSerializer<T>) BitSet_.I;
            } else if (raw.isEnum()) {
                return new Enum_(raw);
            }
        }

        if (type.isArray()) {
            return (NBTSerializer<T>) arraySerializer(method, (TypeToken<Object[]>) type, registry);
        }

        ByteStreamSerializer<T> byteStreamSerializer = registry.getByteStreamSerializer(method, type);
        if (byteStreamSerializer != null) {
            return ByteStreamAsNBTSerializer.create(byteStreamSerializer);
        }

        if (Serializable.class.isAssignableFrom(raw)) {
            return new JavaSerializationShim(type);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T, C extends Collection<T>> IntFunction<C> collectionConstructor(Class<?> rawType, TypeToken<C> token) {
        if (rawType == ArrayList.class || rawType == List.class || rawType == Collection.class) {
            return (IntFunction<C>) (IntFunction<ArrayList<T>>) ArrayList::new;
        } else if (rawType == HashSet.class || rawType == Set.class) {
            return (IntFunction<C>) (IntFunction<HashSet<T>>) Sets::newHashSetWithExpectedSize;
        } else {
            // TODO
            return null;
        }
    }

    @SuppressWarnings("unchecked")

    private static <T> NBTSerializer<T[]> arraySerializer(SerializationMethod method, TypeToken<T[]> type, SerializerRegistry registry) {
        SerializationMethod selectedMethod = method == null ? VALUE : method;

        NBTSerializer<T> elementSerializer = registry.getNbtSerializer(selectedMethod, (TypeToken<T>) type.getComponentType());

        if (elementSerializer == null) {
            return null;
        }
        if (method == VALUE) {
            return ArrayInstanceSerializer.create(type, elementSerializer);
        } else { // method == CONTENTS
            return ArrayContentsSerializer.create(elementSerializer);
        }
    }

    private static final class Enum_<E extends Enum<E>> implements NBTSerializer.Value<E> {

        private static final String NULL_VALUE = "0"; // this must not be a valid Java identifier, to avoid collisions
        private final Class<E> enumClass;

        Enum_(Class<E> enumClass) {
            this.enumClass = enumClass;
        }

        @Override
        public Characteristics characteristics() {
            return new Characteristics(VALUE, true, NBT.Tag.STRING);
        }

        @Override
        public E read(NBTBase nbt) throws SerializationException {
            String s = ((NBTTagString) nbt).getString();
            return s.equals(NULL_VALUE) ? null : Enum.valueOf(enumClass, s);
        }

        @Override
        public NBTBase write(E value) {
            return new NBTTagString(value == null ? NULL_VALUE : value.name());
        }
    }

    private enum Z implements NBTSerializer.Value<Boolean> {

        I;

        private static final byte NULL = -1, TRUE = 1, FALSE = 0;

        @Override
        public Characteristics characteristics() {
            return new Characteristics(VALUE, true, NBT.Tag.BYTE);
        }

        @Override
        public Boolean read(NBTBase nbt) {
            byte b = ((NBTTagByte) nbt).getByte();
            return b == NULL ? null : b == TRUE;
        }

        @Override
        public NBTBase write(Boolean value) {
            return new NBTTagByte(value == null ? NULL : value ? TRUE : FALSE);
        }

        @Override
        public void read(NBTBase nbt, PropertyAccess<Boolean> property, Object obj) {
            byte b = ((NBTTagByte) nbt).getByte();
            if (b == NULL) {
                property.set(obj, null);
            } else {
                property.setBoolean(obj, b == TRUE);
            }
        }

        @Override
        public NBTBase write(PropertyAccess<Boolean> property, Object obj) {
            return new NBTTagByte(property.isNull(obj) ? NULL : property.getBoolean(obj) ? TRUE : FALSE);
        }

    }

    private enum B implements NBTSerializer.Value<Byte> {

        I;

        @Override
        public Characteristics characteristics() {
            return new Characteristics(VALUE, false, NBT.Tag.BYTE);
        }

        @Override
        public Byte read(NBTBase nbt) {
            return ((NBTTagByte) nbt).getByte();
        }

        @Override
        public NBTBase write(Byte value) {
            return new NBTTagByte(value);
        }

        @Override
        public void read(NBTBase nbt, PropertyAccess<Byte> property, Object obj) {
            property.setByte(obj, ((NBTTagByte) nbt).getByte());
        }

        @Override
        public NBTBase write(PropertyAccess<Byte> property, Object obj) {
            return new NBTTagByte(property.getByte(obj));
        }

    }

    private enum S implements NBTSerializer.Value<Short> {

        I;

        @Override
        public Characteristics characteristics() {
            return new Characteristics(VALUE, false, NBT.Tag.SHORT);
        }

        @Override
        public Short read(NBTBase nbt) {
            return ((NBTTagShort) nbt).getShort();
        }

        @Override
        public NBTBase write(Short value) {
            return new NBTTagShort(value);
        }

        @Override
        public void read(NBTBase nbt, PropertyAccess<Short> property, Object obj) {
            property.setShort(obj, ((NBTTagShort) nbt).getShort());
        }

        @Override
        public NBTBase write(PropertyAccess<Short> property, Object obj) {
            return new NBTTagShort(property.getShort(obj));
        }

    }

    private enum C implements NBTSerializer.Value<Character> {

        I;

        @Override
        public Characteristics characteristics() {
            return new Characteristics(VALUE, false, NBT.Tag.SHORT);
        }

        @Override
        public Character read(NBTBase nbt) {
            return (char) ((NBTTagShort) nbt).getShort();
        }

        @Override
        public NBTBase write(Character value) {
            return new NBTTagShort((short) (char) value);
        }

        @Override
        public void read(NBTBase nbt, PropertyAccess<Character> property, Object obj) {
            property.setChar(obj, (char) ((NBTTagShort) nbt).getShort());
        }

        @Override
        public NBTBase write(PropertyAccess<Character> property, Object obj) {
            return new NBTTagShort((short) property.getChar(obj));
        }

    }

    private enum I implements NBTSerializer.Value<Integer> {

        I;

        @Override
        public Characteristics characteristics() {
            return new Characteristics(VALUE, false, NBT.Tag.INT);
        }

        @Override
        public Integer read(NBTBase nbt) {
            return ((NBTTagInt) nbt).getInt();
        }

        @Override
        public NBTBase write(Integer value) {
            return new NBTTagInt(value);
        }

        @Override
        public void read(NBTBase nbt, PropertyAccess<Integer> property, Object obj) {
            property.setInt(obj, ((NBTTagInt) nbt).getInt());
        }

        @Override
        public NBTBase write(PropertyAccess<Integer> property, Object obj) {
            return new NBTTagInt(property.getInt(obj));
        }

    }

    private enum L implements NBTSerializer.Value<Long> {

        I;

        @Override
        public Characteristics characteristics() {
            return new Characteristics(VALUE, false, NBT.Tag.LONG);
        }

        @Override
        public Long read(NBTBase nbt) {
            return ((NBTTagLong) nbt).getLong();
        }

        @Override
        public NBTBase write(Long value) {
            return new NBTTagLong(value);
        }

        @Override
        public void read(NBTBase nbt, PropertyAccess<Long> property, Object obj) {
            property.setLong(obj, ((NBTTagLong) nbt).getLong());
        }

        @Override
        public NBTBase write(PropertyAccess<Long> property, Object obj) {
            return new NBTTagLong(property.getLong(obj));
        }

    }

    private enum F implements NBTSerializer.Value<Float> {

        I;

        @Override
        public Characteristics characteristics() {
            return new Characteristics(VALUE, false, NBT.Tag.FLOAT);
        }

        @Override
        public Float read(NBTBase nbt) {
            return ((NBTTagFloat) nbt).getFloat();
        }

        @Override
        public NBTBase write(Float value) {
            return new NBTTagFloat(value);
        }

        @Override
        public void read(NBTBase nbt, PropertyAccess<Float> property, Object obj) {
            property.setFloat(obj, ((NBTTagFloat) nbt).getFloat());
        }

        @Override
        public NBTBase write(PropertyAccess<Float> property, Object obj) {
            return new NBTTagFloat(property.getFloat(obj));
        }
    }

    private enum D implements NBTSerializer.Value<Double> {

        I;

        @Override
        public Characteristics characteristics() {
            return new Characteristics(VALUE, false, NBT.Tag.DOUBLE);
        }

        @Override
        public Double read(NBTBase nbt) {
            return ((NBTTagDouble) nbt).getDouble();
        }

        @Override
        public NBTBase write(Double value) {
            return new NBTTagDouble(value);
        }

        @Override
        public void read(NBTBase nbt, PropertyAccess<Double> property, Object obj) {
            property.setDouble(obj, ((NBTTagDouble) nbt).getDouble());
        }

        @Override
        public NBTBase write(PropertyAccess<Double> property, Object obj) {
            return new NBTTagDouble(property.getDouble(obj));
        }
    }

    private enum ZA implements NBTSerializer.Value<boolean[]> {

        I;

        @Override
        public Characteristics characteristics() {
            return new Characteristics(VALUE, false, NBT.Tag.COMPOUND);
        }

        @Override
        public boolean[] read(NBTBase nbt) {
            NBTTagCompound c = (NBTTagCompound) nbt;
            byte[] packed = c.getByteArray("a");
            int boolLen = c.getInteger("l");

            boolean[] result = new boolean[boolLen];
            for (int i = 0; i < boolLen; i++) {
                if ((packed[i >> 3] & (1 << (i & 7))) != 0) {
                    result[i] = true;
                }
            }
            return result;
        }

        @Override
        public NBTBase write(boolean[] value) {
            int boolLen = value.length;
            int byteLen = (boolLen + 7) >>> 3; // divide by 8 and round up
            byte[] packed = new byte[byteLen];
            for (int i = 0; i < boolLen; i++) {
                if (value[i]) {
                    packed[i >> 3] |= 1 << (i & 7);
                }
            }
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("l", boolLen);
            nbt.setByteArray("a", packed);
            return nbt;
        }
    }

    private enum BA implements NBTSerializer.Value<byte[]> {

        I;

        @Override
        public Characteristics characteristics() {
            return new Characteristics(VALUE, false, NBT.Tag.BYTE_ARRAY);
        }

        @Override
        public byte[] read(NBTBase nbt) {
            return ((NBTTagByteArray) nbt).getByteArray();
        }

        @Override
        public NBTBase write(byte[] value) {
            return new NBTTagByteArray(value);
        }
    }

    private enum SA implements NBTSerializer.Value<short[]> {

        I;

        @Override
        public Characteristics characteristics() {
            return new Characteristics(VALUE, false, NBT.Tag.BYTE_ARRAY);
        }

        @Override
        public short[] read(NBTBase nbt) {
            byte[] bytes = ((NBTTagByteArray) nbt).getByteArray();
            int byteLen = bytes.length;
            int shortLen = byteLen >>> 1;
            short[] shorts = new short[shortLen];
            for (int i = 0; i < shortLen; i++) {
                int bi = i << 1;
                shorts[i] = (short) ((bytes[bi] & 0xFF) | (bytes[bi + 1] << 8));
            }
            return shorts;
        }

        @Override
        public NBTBase write(short[] value) {
            int shortLen = value.length;
            int byteLen = shortLen << 1;
            byte[] bytes = new byte[byteLen];
            for (int i = 0; i < shortLen; i++) {
                int bi = i << 1;
                short s = value[i];
                bytes[bi] = (byte) s;
                bytes[bi + 1] = (byte) (s >>> 8);
            }
            return new NBTTagByteArray(bytes);
        }
    }

    private enum CA implements NBTSerializer.Value<char[]> {

        I;

        @Override
        public Characteristics characteristics() {
            return new Characteristics(VALUE, false, NBT.Tag.BYTE_ARRAY);
        }

        @Override
        public char[] read(NBTBase nbt) {
            byte[] bytes = ((NBTTagByteArray) nbt).getByteArray();
            int byteLen = bytes.length;
            int charLen = byteLen >>> 1;
            char[] shorts = new char[charLen];
            for (int i = 0; i < charLen; i++) {
                int bi = i << 1;
                shorts[i] = (char) ((bytes[bi] & 0xFF) | (bytes[bi + 1] << 8));
            }
            return shorts;
        }

        @Override
        public NBTBase write(char[] value) {
            int charLen = value.length;
            int byteLen = charLen << 1;
            byte[] bytes = new byte[byteLen];
            for (int i = 0; i < charLen; i++) {
                int bi = i << 1;
                char s = value[i];
                bytes[bi] = (byte) s;
                bytes[bi + 1] = (byte) (s >>> 8);
            }
            return new NBTTagByteArray(bytes);
        }
    }

    private enum IA implements NBTSerializer.Value<int[]> {

        I;

        @Override
        public Characteristics characteristics() {
            return new Characteristics(VALUE, false, NBT.Tag.INT_ARRAY);
        }

        @Override
        public int[] read(NBTBase nbt) {
            return ((NBTTagIntArray) nbt).getIntArray();
        }

        @Override
        public NBTBase write(int[] value) {
            return new NBTTagIntArray(value);
        }
    }

    private enum LA implements NBTSerializer.Value<long[]> {

        I;

        @Override
        public Characteristics characteristics() {
            return new Characteristics(VALUE, false, NBT.Tag.BYTE_ARRAY);
        }

        @Override
        public long[] read(NBTBase nbt) {
            byte[] bytes = ((NBTTagByteArray) nbt).getByteArray();
            int byteLen = bytes.length;
            int longLen = byteLen >>> 3;
            long[] longs = new long[longLen];
            for (int i = 0; i < longLen; i++) {
                longs[i] = decodeLong(bytes, i << 3);
            }
            return longs;
        }

        @Override
        public NBTBase write(long[] value) {
            int longLen = value.length;
            int byteLen = longLen << 3;
            byte[] bytes = new byte[byteLen];
            for (int i = 0; i < longLen; i++) {
                encodeLong(bytes, i << 3, value[i]);
            }
            return new NBTTagByteArray(bytes);
        }
    }

    static void encodeLong(byte[] b, int bi, long l) {
        // @formatter:off
        b[bi    ] = (byte) (l >>> 56);
        b[bi + 1] = (byte) (l >>> 48);
        b[bi + 2] = (byte) (l >>> 40);
        b[bi + 3] = (byte) (l >>> 32);
        b[bi + 4] = (byte) (l >>> 24);
        b[bi + 5] = (byte) (l >>> 16);
        b[bi + 6] = (byte) (l >>> 8 );
        b[bi + 7] = (byte) (l       );
        // @formatter:on
    }

    static long decodeLong(byte[] b, int bi) {
        return // @formatter:off
                (b[bi    ] & 0xFFL) << 56
              | (b[bi + 1] & 0xFFL) << 48
              | (b[bi + 2] & 0xFFL) << 40
              | (b[bi + 3] & 0xFFL) << 32
              | (b[bi + 4] & 0xFFL) << 24
              | (b[bi + 5] & 0xFFL) << 16
              | (b[bi + 6] & 0xFFL) << 8
              | (b[bi + 7] & 0xFFL);
        // @formatter:on
    }

    private enum FA implements NBTSerializer.Value<float[]> {

        I;

        @Override
        public Characteristics characteristics() {
            return new Characteristics(VALUE, false, NBT.Tag.INT_ARRAY);
        }

        @Override
        public float[] read(NBTBase nbt) {
            int[] ints = ((NBTTagIntArray) nbt).getIntArray();
            int len = ints.length;
            float[] floats = new float[len];
            for (int i = 0; i < len; i++) {
                floats[i] = Float.intBitsToFloat(ints[i]);
            }
            return floats;
        }

        @Override
        public NBTBase write(float[] value) {
            int len = value.length;
            int[] ints = new int[len];
            for (int i = 0; i < len; i++) {
                ints[i] = Float.floatToRawIntBits(value[i]);
            }
            return new NBTTagIntArray(ints);
        }
    }

    private enum DA implements NBTSerializer.Value<double[]> {

        I;

        @Override
        public Characteristics characteristics() {
            return new Characteristics(VALUE, false, NBT.Tag.BYTE_ARRAY);
        }

        @Override
        public double[] read(NBTBase nbt) {
            byte[] bytes = ((NBTTagByteArray) nbt).getByteArray();
            int byteLen = bytes.length;
            int doubleLen = byteLen >>> 3;
            double[] doubles = new double[doubleLen];
            for (int i = 0; i < doubleLen; i++) {
                doubles[i] = Double.longBitsToDouble(decodeLong(bytes, i << 3));
            }
            return doubles;
        }

        @Override
        public NBTBase write(double[] value) {
            int doubleLen = value.length;
            int byteLen = doubleLen << 3;
            byte[] bytes = new byte[byteLen];
            for (int i = 0; i < doubleLen; i++) {
                encodeLong(bytes, i << 3, Double.doubleToRawLongBits(value[i]));
            }
            return new NBTTagByteArray(bytes);
        }
    }

    private enum String_ implements NBTSerializer.Value<String> {

        I;

        @Override
        public Characteristics characteristics() {
            return new Characteristics(VALUE, false, NBT.Tag.STRING);
        }

        @Override
        public String read(NBTBase nbt) {
            return ((NBTTagString) nbt).getString();
        }

        @Override
        public NBTBase write(String value) {
            return new NBTTagString(value);
        }
    }

    private enum UUID_ implements NBTSerializer.Value<UUID> {

        I;

        @Override
        public Characteristics characteristics() {
            return new Characteristics(VALUE, false, NBT.Tag.COMPOUND);
        }

        @Override
        public UUID read(NBTBase nbt) {
            NBTTagCompound c = (NBTTagCompound) nbt;
            return c.getUniqueId("");
        }

        @Override
        public NBTBase write(UUID value) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setUniqueId("", value);
            return nbt;
        }
    }

    private enum BigInteger_ implements NBTSerializer.Value<BigInteger> {

        I;

        @Override
        public Characteristics characteristics() {
            return new Characteristics(VALUE, false, NBT.Tag.BYTE_ARRAY);
        }

        @Override
        public BigInteger read(NBTBase nbt) {
            return new BigInteger(((NBTTagByteArray) nbt).getByteArray());
        }

        @Override
        public NBTBase write(BigInteger value) {
            return new NBTTagByteArray(value.toByteArray());
        }
    }

    private enum BigDecimal_ implements NBTSerializer.Value<BigDecimal> {

        I;

        @Override
        public Characteristics characteristics() {
            return new Characteristics(VALUE, false, NBT.Tag.COMPOUND);
        }

        @Override
        public BigDecimal read(NBTBase nbt) {
            NBTTagCompound c = (NBTTagCompound) nbt;
            int scale = c.getInteger("s");
            BigInteger unscaledVal = new BigInteger(c.getByteArray("u"));
            return new BigDecimal(unscaledVal, scale);
        }

        @Override
        public NBTBase write(BigDecimal value) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("s", value.scale());
            nbt.setByteArray("u", value.unscaledValue().toByteArray());
            return nbt;
        }
    }

    private enum URL_ implements NBTSerializer.Value<URL> {

        I;

        @Override
        public Characteristics characteristics() {
            return new Characteristics(VALUE, false, NBT.Tag.STRING);
        }

        @Override
        public URL read(NBTBase nbt) throws SerializationException {
            try {
                return new URL(((NBTTagString) nbt).getString());
            } catch (MalformedURLException e) {
                throw new SerializationException(e);
            }
        }

        @Override
        public NBTBase write(URL value) {
            return new NBTTagString(value.toExternalForm());
        }
    }

    private enum URI_ implements NBTSerializer.Value<URI> {

        I;

        @Override
        public Characteristics characteristics() {
            return new Characteristics(VALUE, false, NBT.Tag.STRING);
        }

        @Override
        public URI read(NBTBase nbt) throws SerializationException {
            try {
                return new URI(((NBTTagString) nbt).getString());
            } catch (URISyntaxException e) {
                throw new SerializationException(e);
            }
        }

        @Override
        public NBTBase write(URI value) {
            return new NBTTagString(value.toString());
        }
    }

    private enum BitSet_ implements NBTSerializer.Value<BitSet> {

        I;

        @Override
        public Characteristics characteristics() {
            return new Characteristics(VALUE, false, NBT.Tag.BYTE_ARRAY);
        }

        @Override
        public BitSet read(NBTBase nbt) throws SerializationException {
            return BitSet.valueOf(((NBTTagByteArray) nbt).getByteArray());
        }

        @Override
        public NBTBase write(BitSet value) {
            return new NBTTagByteArray(value.toByteArray());
        }
    }

}
