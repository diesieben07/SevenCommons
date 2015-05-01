package de.take_weiland.mods.commons.nbt;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * @author diesieben07
 */
public final class NBTData {

    public static final byte NULL = -1;
    public static final String NULL_KEY = "_sc$null";

    private NBTData() { }

    /**
     * <p>Write the given String to NBT.</p>
     * @param s the String
     * @return NBT data
     */
    @Nonnull
    public static NBTBase writeString(@Nullable String s) {
        return s == null ? serializedNull() : new NBTTagString(s);
    }

    /**
     * <p>Read a String from NBT.</p>
     * @param nbt the NBT data
     * @return a String
     */
    @Nullable
    public static String readString(@Nullable NBTBase nbt) {
        return isSerializedNull(nbt) ? null : ((NBTTagString) nbt).func_150285_a_();
    }

    @Nonnull
    public static NBTBase writeBlock(@Nullable Block block) {
        if (block == null) {
            return serializedNull();
        } else {
            return new NBTTagString(Block.blockRegistry.getNameForObject(block));
        }
    }

    @Nullable
    public static Block readBlock(@Nullable NBTBase nbt) {
        if (isSerializedNull(nbt)) {
            return null;
        } else {
            return Block.getBlockFromName(((NBTTagString) nbt).func_150285_a_());
        }
    }

    @Nonnull
    public static NBTBase writeItem(@Nullable Item item) {
        if (item == null) {
            return serializedNull();
        } else {
            return new NBTTagString(Item.itemRegistry.getNameForObject(item));
        }
    }

    @Nullable
    public static Item readItem(@Nullable NBTBase nbt) {
        if (isSerializedNull(nbt)) {
            return null;
        } else {
            return (Item) Item.itemRegistry.getObject(((NBTTagString) nbt).func_150285_a_());
        }
    }

    /**
     * <p>Write the given UUID to NBT.</p>
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
     * @param nbt the NBT data
     * @return an UUID
     */
    @Nullable
    public static UUID readUUID(@Nullable NBTBase nbt) {
        if (isSerializedNull(nbt) || nbt.getId() != NBT.TAG_LIST) {
            return null;
        } else {
            List<NBTBase> asList = NBT.asList((NBTTagList) nbt);
            return new UUID(((NBTTagLong) asList.get(0)).func_150291_c(), ((NBTTagLong) asList.get(1)).func_150291_c());
        }
    }

    /**
     * <p>Write the given ItemStack to NBT.</p>
     * @param stack the ItemStack
     * @return NBT data
     */
    @Nonnull
    public static NBTBase writeItemStack(@Nullable ItemStack stack) {
        return stack == null ? serializedNull() : stack.writeToNBT(new NBTTagCompound());
    }

    /**
     * <p>Read an ItemStack from NBT.</p>
     * @param nbt the NBT data
     * @return an ItemStack
     */
    @Nullable
    public static ItemStack readItemStack(@Nullable NBTBase nbt) {
        return isSerializedNull(nbt) ? null : ItemStack.loadItemStackFromNBT((NBTTagCompound) nbt);
    }

    /**
     * <p>Write the given FluidStack to NBT.</p>
     * @param stack the FluidStack
     * @return NBT data
     */
    @Nonnull
    public static NBTBase writeFluidStack(@Nullable FluidStack stack) {
        return stack == null ? serializedNull() : stack.writeToNBT(new NBTTagCompound());
    }

    /**
     * <p>Read a FluidStack from NBT.</p>
     * @param nbt the NBT data
     * @return a FluidStack
     */
    @Nullable
    public static FluidStack readFluidStack(@Nullable NBTBase nbt) {
        return isSerializedNull(nbt) ? null : FluidStack.loadFluidStackFromNBT((NBTTagCompound) nbt);
    }

    /**
     * <p>Write the given Enum to NBT.</p>
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
     * @param nbt the NBT data
     * @param clazz the Class of the Enum to read
     * @return an Enum
     */
    @Nullable
    public static <E extends Enum<E>> E readEnum(@Nullable NBTBase nbt, Class<E> clazz) {
        if (isSerializedNull(nbt)) {
            return null;
        } else {
            return Enum.valueOf(clazz, ((NBTTagString) nbt).func_150285_a_());
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
        if (isSerializedNull(nbt)) {
            return null;
        } else {
            EnumSet<E> enumSet = EnumSet.noneOf(enumClass);
            List<NBTBase> list = NBT.asList(((NBTTagList) nbt));
            for (int i = list.size() - 1; i >= 0; i--) {
                enumSet.add(Enum.valueOf(enumClass, ((NBTTagString) list.get(i)).func_150285_a_()));
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
        if (isSerializedNull(nbt)) {
            return null;
        } else {
            return BitSet.valueOf(((NBTTagByteArray) nbt).func_150292_c());
        }
    }

    /**
     * <p>Get an NBT Tag that represents {@code null}.</p>
     * @return NBT data
     * @see #isSerializedNull(net.minecraft.nbt.NBTBase)
     */
    @Nonnull
    public static NBTBase serializedNull() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte(NULL_KEY, NULL);
        return nbt;
    }

    /**
     * <p>Check if the given NBT Tag is null or represents a serialized {@code null} reference.</p>
     * @param nbt the NBT data
     * @return true if the NBT data represents null
     * @see #serializedNull()
     */
    @Contract("null->true")
    public static boolean isSerializedNull(@Nullable NBTBase nbt) {
        return nbt == null || nbt.getId() == NBT.TAG_COMPOUND && ((NBTTagCompound) nbt).getByte(NULL_KEY) == NULL;
    }

}
