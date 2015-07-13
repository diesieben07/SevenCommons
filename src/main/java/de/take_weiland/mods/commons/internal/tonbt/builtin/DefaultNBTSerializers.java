package de.take_weiland.mods.commons.internal.tonbt.builtin;

import com.google.common.primitives.Primitives;
import de.take_weiland.mods.commons.nbt.NBT;
import de.take_weiland.mods.commons.nbt.NBTSerializer;
import de.take_weiland.mods.commons.nbt.NBTSerializerFactory;
import de.take_weiland.mods.commons.reflect.Property;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author diesieben07
 */
@ParametersAreNonnullByDefault
public final class DefaultNBTSerializers implements NBTSerializerFactory {

    @Override
    public <T> NBTSerializer<T> get(Property<T> property) {
        Class<?> raw = Primitives.unwrap(property.getRawType()); // the primitive handlers handle boxes as well
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
        } else if (CharSequence.class.isAssignableFrom(raw)) {
            result = ForCharSeq.INSTANCE;
        } else if (raw.isEnum()) {
            result = EnumSerializer.get(raw);
        } else {
            result = null;
        }
        //noinspection unchecked
        return (NBTSerializer<T>) result;
    }

    private enum ForBool implements NBTSerializer.ForValue<Boolean> {
        INSTANCE;

        private static final byte TRUE = 1;
        private static final byte FALSE = 0;

        @Override
        public NBTBase write(@Nonnull Boolean value) {
            return new NBTTagByte(value ? TRUE : FALSE);
        }

        @Override
        public Boolean read(NBTBase nbt) {
            return nbt.getId() == NBT.TAG_BYTE && ((NBTTagByte) nbt).getByte() == TRUE;
        }
    }

    private enum ForByte implements NBTSerializer.ForValue<Byte> {
        INSTANCE;

        @Override
        public NBTBase write(@Nonnull Byte value) {
            return new NBTTagByte(value);
        }

        @Override
        public Byte read(NBTBase nbt) {
            return nbt.getId() == NBT.TAG_BYTE ? ((NBTTagByte) nbt).getByte() : 0;
        }
    }

    private enum ForShort implements NBTSerializer.ForValue<Short> {
        INSTANCE;


        @Override
        public NBTBase write(@Nonnull Short value) {
            return new NBTTagShort(value);
        }

        @Override
        public Short read(NBTBase nbt) {
            return nbt.getId() == NBT.TAG_SHORT ? ((NBTTagShort) nbt).getShort() : 0;
        }
    }

    private enum ForChar implements NBTSerializer.ForValue<Character> {

        INSTANCE;

        @Override
        public NBTBase write(@Nonnull Character value) {
            return new NBTTagShort((short) value.charValue());
        }

        @Override
        public Character read(NBTBase nbt) {
            return nbt.getId() == NBT.TAG_SHORT ? (char) ((NBTTagShort) nbt).getShort() : 0;
        }
    }

    private enum ForInt implements NBTSerializer.ForValue<Integer> {
        INSTANCE;

        @Override
        public NBTBase write(@Nonnull Integer value) {
            return new NBTTagInt(value);
        }

        @Override
        public Integer read(NBTBase nbt) {
            return nbt.getId() == NBT.TAG_INT ? ((NBTTagInt) nbt).getInt() : 0;
        }
    }

    private enum ForLong implements NBTSerializer.ForValue<Long> {

        INSTANCE;

        @Override
        public NBTBase write(@Nonnull Long value) {
            return new NBTTagLong(value);
        }

        @Override
        public Long read(NBTBase nbt) {
            return nbt.getId() == NBT.TAG_LONG ? ((NBTTagLong) nbt).getLong() : 0;
        }
    }

    private enum ForFloat implements NBTSerializer.ForValue<Float> {

        INSTANCE;

        @Override
        public NBTBase write(@Nonnull Float value) {
            return new NBTTagFloat(value);
        }

        @Override
        public Float read(NBTBase nbt) {
            return nbt.getId() == NBT.TAG_FLOAT ? ((NBTTagFloat) nbt).getFloat() : 0f;
        }
    }

    private enum ForDouble implements NBTSerializer.ForValue<Double> {

        INSTANCE;

        @Override
        public NBTBase write(@Nonnull Double value) {
            return new NBTTagDouble(value);
        }

        @Override
        public Double read(NBTBase nbt) {
            return nbt.getId() == NBT.TAG_DOUBLE ? ((NBTTagDouble) nbt).getDouble() : 0d;
        }
    }

    private enum ForCharSeq implements NBTSerializer.ForValue<CharSequence> {

        INSTANCE;

        @Override
        public NBTBase write(@Nonnull CharSequence value) {
            return new NBTTagString(value.toString());
        }

        @Override
        public CharSequence read(NBTBase nbt) {
            return nbt.getId() == NBT.TAG_STRING ? ((NBTTagString) nbt).getString() : null;
        }
    }

    private enum ForItemStack implements NBTSerializer.ForValue<ItemStack> {

        INSTANCE;

        @Override
        public ItemStack read(NBTBase nbt) {
            return nbt.getId() == NBT.TAG_COMPOUND ? ItemStack.loadItemStackFromNBT((NBTTagCompound) nbt) : null;
        }

        @Override
        public NBTBase write(ItemStack value) {
            return value.writeToNBT(new NBTTagCompound());
        }
    }

    private enum ForFluidStack implements NBTSerializer.ForValue<FluidStack> {

        INSTANCE;

        @Override
        public FluidStack read(NBTBase nbt) {
            return nbt.getId() == NBT.TAG_COMPOUND ? FluidStack.loadFluidStackFromNBT((NBTTagCompound) nbt) : null;
        }

        @Override
        public NBTBase write(FluidStack value) {
            return value.writeToNBT(new NBTTagCompound());
        }
    }

    private enum ForFluidTank implements NBTSerializer.ForContainer<FluidTank> {

        INSTANCE;


        @Override
        public void read(NBTBase nbt, FluidTank tank) {
            if (nbt.getId() == NBT.TAG_COMPOUND) {
                tank.readFromNBT((NBTTagCompound) nbt);
            }
        }

        @Override
        public NBTBase write(FluidTank value) {
            return value.writeToNBT(new NBTTagCompound());
        }
    }

}
