package de.take_weiland.mods.commons.nbt;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.UUID;

/**
 * @author diesieben07
 */
public final class NBTData {

    public static final byte NULL = -1;
    public static final String NULL_KEY = "_sc$null";
    public static final String WRAPPED_KEY = "_sc$wrap";

    private NBTData() {
    }

    /**
     * <p>Write the given String to NBT.</p>
     *
     * @param s the String
     * @return NBT data
     */
    @Nonnull
    public static NBTBase writeString(@Nullable String s) {
        return s == null ? serializedNull() : new NBTTagString(s);
    }

    /**
     * <p>Read a String from NBT.</p>
     *
     * @param nbt the NBT data
     * @return a String
     */
    @Nullable
    public static String readString(@Nullable NBTBase nbt) {
        if (isSerializedNull(nbt, NBT.TAG_STRING)) {
            return null;
        } else {
            return ((NBTTagString) nbt).getString();
        }
    }

    @Nonnull
    public static NBTBase writeBlock(@Nullable Block block) {
        return writeRegistryEntry(block);
    }

    @Nullable
    public static Block readBlock(@Nullable NBTBase nbt) {
        return readRegistryEntry(nbt, ForgeRegistries.BLOCKS);
    }

    @Nonnull
    public static NBTBase writeItem(@Nullable Item item) {
        return writeRegistryEntry(item);
    }

    @Nullable
    public static Item readItem(@Nullable NBTBase nbt) {
        return readRegistryEntry(nbt, ForgeRegistries.ITEMS);
    }

    @Nonnull
    public static NBTBase writeRegistryEntry(@Nullable IForgeRegistryEntry<?> entry) {
        if (entry == null) {
            return serializedNull();
        } else {
            return new NBTTagString(entry.getRegistryName().toString());
        }
    }

    @Nullable
    public static <V extends IForgeRegistryEntry<V>> V readRegistryEntry(@Nullable NBTBase nbt, IForgeRegistry<V> registry) {
        if (isSerializedNull(nbt, NBT.TAG_STRING)) {
            return null;
        } else {
            return registry.getValue(new ResourceLocation(((NBTTagString) nbt).getString()));
        }
    }

    /**
     * <p>Write the given UUID to NBT.</p>
     *
     * @param uuid the UUID
     * @return NBT data
     */
    @Nonnull
    public static NBTBase writeUUID(@Nullable UUID uuid) {
        if (uuid == null) {
            return serializedNull();
        } else {
            NBTTagList nbt = new NBTTagList();
            nbt.appendTag(new NBTTagLong(uuid.getMostSignificantBits()));
            nbt.appendTag(new NBTTagLong(uuid.getLeastSignificantBits()));
            return nbt;
        }
    }

    /**
     * <p>Read an UUID from NBT.</p>
     *
     * @param nbt the NBT data
     * @return an UUID
     */
    @Nullable
    public static UUID readUUID(@Nullable NBTBase nbt) {
        if (isSerializedNull(nbt, NBT.TAG_LIST)) {
            return null;
        } else {
            NBTTagList list = (NBTTagList) nbt;
            if (list.tagCount() != 2) {
                return null;
            }
            NBTBase msb = list.get(0);
            if (msb.getId() != NBT.TAG_LONG) {
                return null;
            }
            NBTBase lsb = list.get(1);
            if (lsb.getId() != NBT.TAG_LONG) {
                return null;
            }
            return new UUID(((NBTTagLong) msb).getLong(), ((NBTTagLong) lsb).getLong());
        }
    }

    /**
     * <p>Write the given ItemStack to NBT.</p>
     *
     * @param stack the ItemStack
     * @return NBT data
     */
    @Nonnull
    public static NBTBase writeItemStack(@Nullable ItemStack stack) {
        return stack == null ? serializedNull() : stack.writeToNBT(new NBTTagCompound());
    }

    /**
     * <p>Read an ItemStack from NBT.</p>
     *
     * @param nbt the NBT data
     * @return an ItemStack
     */
    @Nullable
    public static ItemStack readItemStack(@Nullable NBTBase nbt) {
        return isSerializedNull(nbt, NBT.TAG_COMPOUND) ? null : new ItemStack((NBTTagCompound) nbt);
    }

    /**
     * <p>Write the given FluidStack to NBT.</p>
     *
     * @param stack the FluidStack
     * @return NBT data
     */
    @Nonnull
    public static NBTBase writeFluidStack(@Nullable FluidStack stack) {
        return stack == null ? serializedNull() : stack.writeToNBT(new NBTTagCompound());
    }

    /**
     * <p>Read a FluidStack from NBT.</p>
     *
     * @param nbt the NBT data
     * @return a FluidStack
     */
    @Nullable
    public static FluidStack readFluidStack(@Nullable NBTBase nbt) {
        return isSerializedNull(nbt, NBT.TAG_COMPOUND) ? null : FluidStack.loadFluidStackFromNBT((NBTTagCompound) nbt);
    }

    /**
     * <p>Write the given Enum to NBT.</p>
     *
     * @param e the Enum
     * @return NBT data
     */
    @Nonnull
    public static <E extends Enum<E>> NBTBase writeEnum(@Nullable E e) {
        if (e == null) {
            return serializedNull();
        } else {
            return new NBTTagString(e.name());
        }
    }

    /**
     * <p>Read an Enum of the given Class from NBT.</p>
     *
     * @param nbt   the NBT data
     * @param clazz the Class of the Enum to read
     * @return an Enum
     */
    @Nullable
    public static <E extends Enum<E>> E readEnum(@Nullable NBTBase nbt, Class<E> clazz) {
        if (isSerializedNull(nbt, NBT.TAG_STRING)) {
            return null;
        } else {
            return Enum.valueOf(clazz, ((NBTTagString) nbt).getString());
        }
    }

    @Nonnull
    public static <E extends Enum<E>> NBTBase writeEnumSet(@Nullable EnumSet<E> enumSet) {
        if (enumSet == null) {
            return serializedNull();
        } else {
            NBTTagList list = new NBTTagList();
            for (E e : enumSet) {
                list.appendTag(new NBTTagString(e.name()));
            }
            return list;
        }
    }

    @Nullable
    public static <E extends Enum<E>> EnumSet<E> readEnumSet(@Nullable NBTBase nbt, Class<E> enumClass) {
        if (isSerializedNull(nbt, NBT.TAG_LIST)) {
            return null;
        } else {
            EnumSet<E> enumSet = EnumSet.noneOf(enumClass);
            NBTTagList list = (NBTTagList) nbt;
            for (int i = 0, n = list.tagCount(); i < n; i++) {
                NBTBase e = list.get(i);
                if (e.getId() != NBT.TAG_STRING) {
                    return null;
                }
                enumSet.add(Enum.valueOf(enumClass, ((NBTTagString) e).getString()));
            }
            return enumSet;
        }
    }

    @Nonnull
    public static NBTBase writeBitSet(@Nullable BitSet bitSet) {
        if (bitSet == null) {
            return serializedNull();
        } else {
            return new NBTTagByteArray(bitSet.toByteArray());
        }
    }

    @Nullable
    public static BitSet readBitSet(@Nullable NBTBase nbt) {
        if (isSerializedNull(nbt, NBT.TAG_BYTE_ARRAY)) {
            return null;
        } else {
            return BitSet.valueOf(((NBTTagByteArray) nbt).getByteArray());
        }
    }

    /**
     * <p>Get an NBT Tag that represents {@code null}.</p>
     *
     * @return NBT data
     * @see #isSerializedNull(net.minecraft.nbt.NBTBase)
     */
    @Nonnull
    public static NBTTagCompound serializedNull() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte(NULL_KEY, NULL);
        return nbt;
    }

    public static NBTTagCompound serializedWrapper(NBTBase nbt) {
        if (nbt.getId() == NBT.TAG_COMPOUND) {
            return (NBTTagCompound) nbt;
        } else {
            NBTTagCompound c = new NBTTagCompound();
            c.setTag(WRAPPED_KEY, nbt);
            return c;
        }
    }

    public static NBTBase unwrap(NBTBase nbt) {
        if (nbt.getId() == NBT.TAG_COMPOUND) {
            NBTTagCompound c = (NBTTagCompound) nbt;
            NBTBase tag = c.getTag(WRAPPED_KEY);
            if (tag != null) {
                return tag;
            }
        }
        return nbt;
    }

    /**
     * <p>Check if the given NBT Tag is null or represents a serialized {@code null} reference.</p>
     *
     * @param nbt the NBT data
     * @return true if the NBT data represents null
     * @see #serializedNull()
     */
    @Contract("null->true")
    public static boolean isSerializedNull(@Nullable NBTBase nbt) {
        return nbt == null || nbt.getId() == NBT.TAG_COMPOUND && ((NBTTagCompound) nbt).getByte(NULL_KEY) == NULL;
    }

    public static boolean idMatches(NBTBase nbt, int id) {
        if (id == NBT.TAG_ANY) return true;
        int actualId = nbt.getId();
        return id == NBT.TAG_NUMBER ? actualId >= 1 && actualId <= 6 : actualId == id;
    }

    public static boolean isSerializedNull(@Nullable NBTBase nbt, int id) {
        // null is null
        if (nbt == null) return true;

        int actualId = nbt.getId();

        // serialized null is null
        if (actualId == NBT.TAG_COMPOUND && ((NBTTagCompound) nbt).getByte(NULL_KEY) == NULL) return true;

        return id != NBT.TAG_ANY && (id == NBT.TAG_NUMBER ? actualId >= 1 && actualId <= 6 : actualId == id);

    }

}
