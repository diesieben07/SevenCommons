package de.take_weiland.mods.commons.internal.tonbt.builtin;

import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.nbt.NBTSerializer;
import de.take_weiland.mods.commons.nbt.NBTSerializerFactory;
import de.take_weiland.mods.commons.serialize.Property;
import net.minecraft.nbt.*;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class DefaultNBTSerializers implements NBTSerializerFactory {

    @Override
    public <T> NBTSerializer<T> get(Property<T, ?> typeSpec) {
        Class<?> raw = typeSpec.getRawType();
        NBTSerializer<?> result;
        if (raw == boolean.class) {
            result = ForBool.INSTANCE;
        } else if (raw == byte.class) {
            result = ForByte.INSTANCE;
        } else if (raw == short.class) {
            result = ForShort.INSTANCE;
        } else if (raw == char.class) {
            result = ForChar.INSTANCE;
        } else if (raw == int.class) {
            result = ForInt.INSTANCE;
        } else if (raw == long.class) {
            result = ForLong.INSTANCE;
        } else if (raw == float.class) {
            result = ForFloat.INSTANCE;
        } else if (raw == double.class) {
            result = ForDouble.INSTANCE;
        } else if (raw == String.class) {
            result = ForString.INSTANCE;
        } else if (raw == CharSequence.class) {
            result = ForCharSeq.INSTANCE;
        } else if (raw.isEnum()) {
            result = EnumSerializer.get(raw);
        } else {
            result = null;
        }
        //noinspection unchecked
        return (NBTSerializer<T>) result;
    }

    private enum ForBool implements NBTSerializer<Boolean> {
        INSTANCE;

        private static final byte TRUE = 1;
        private static final byte FALSE = 0;

        @Override
        public NBTBase write(Boolean value) {
            return new NBTTagByte(value ? TRUE : FALSE);
        }

        @Override
        public Boolean read(Boolean value, NBTBase nbt) {
            return nbt.getId() == NBT.TAG_BYTE && ((NBTTagByte) nbt).func_150290_f() == TRUE;
        }
    }

    private enum ForByte implements NBTSerializer<Byte> {
        INSTANCE;

        @Override
        public NBTBase write(Byte value) {
            return new NBTTagByte(value);
        }

        @Override
        public Byte read(Byte value, NBTBase nbt) {
            return nbt.getId() == NBT.TAG_BYTE ? ((NBTTagByte) nbt).func_150290_f() : 0;
        }
    }

    private enum ForShort implements NBTSerializer<Short> {
        INSTANCE;


        @Override
        public NBTBase write(Short value) {
            return new NBTTagShort(value);
        }

        @Override
        public Short read(Short value, NBTBase nbt) {
            return nbt.getId() == NBT.TAG_SHORT ? ((NBTTagShort) nbt).func_150289_e() : 0;
        }
    }

    private enum ForChar implements NBTSerializer<Character> {

        INSTANCE;

        @Override
        public NBTBase write(Character value) {
            return new NBTTagShort((short) value.charValue());
        }

        @Override
        public Character read(Character value, NBTBase nbt) {
            return nbt.getId() == NBT.TAG_SHORT ? (char) ((NBTTagShort) nbt).func_150289_e() : 0;
        }
    }

    private enum ForInt implements NBTSerializer<Integer> {
        INSTANCE;

        @Override
        public NBTBase write(Integer value) {
            return new NBTTagInt(value);
        }

        @Override
        public Integer read(Integer value, NBTBase nbt) {
            return nbt.getId() == NBT.TAG_INT ? ((NBTTagInt) nbt).func_150287_d() : 0;
        }
    }

    private enum ForLong implements NBTSerializer<Long> {

        INSTANCE;

        @Override
        public NBTBase write(Long value) {
            return new NBTTagLong(value);
        }

        @Override
        public Long read(Long value, NBTBase nbt) {
            return nbt.getId() == NBT.TAG_LONG ? ((NBTTagLong) nbt).func_150291_c() : 0;
        }
    }

    private enum ForFloat implements NBTSerializer<Float> {

        INSTANCE;

        @Override
        public NBTBase write(Float value) {
            return new NBTTagFloat(value);
        }

        @Override
        public Float read(Float value, NBTBase nbt) {
            return nbt.getId() == NBT.TAG_FLOAT ? ((NBTTagFloat) nbt).func_150288_h()  : 0f;
        }
    }

    private enum ForDouble implements NBTSerializer<Double> {

        INSTANCE;

        @Override
        public NBTBase write(Double value) {
            return new NBTTagDouble(value);
        }

        @Override
        public Double read(Double value, NBTBase nbt) {
            return nbt.getId() == NBT.TAG_DOUBLE ? ((NBTTagDouble) nbt).func_150286_g() : 0d;
        }
    }

    private enum ForString implements NBTSerializer<String> {
        INSTANCE;

        @Override
        public NBTBase write(String value) {
            return value.isEmpty() ? new NBTTagByte((byte) 0) : new NBTTagString(value);
        }

        @Override
        public String read(String value, NBTBase nbt) {
            return nbt.getId() == NBT.TAG_STRING ? ((NBTTagString) nbt).func_150285_a_() : "";
        }
    }

    private enum ForCharSeq implements NBTSerializer<CharSequence> {

        INSTANCE;

        @Override
        public NBTBase write(CharSequence value) {
            return new NBTTagString(value.toString());
        }

        @Override
        public CharSequence read(CharSequence value, NBTBase nbt) {
            return nbt.getId()== NBT.TAG_STRING ? ((NBTTagString) nbt).func_150285_a_() : null;
        }
    }

}
