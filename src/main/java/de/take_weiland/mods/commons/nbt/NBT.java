package de.take_weiland.mods.commons.nbt;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import de.take_weiland.mods.commons.asm.MCPNames;
import de.take_weiland.mods.commons.internal.SRGConstants;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.invoke.MethodHandles.publicLookup;

/**
 * <p>Utility methods regarding NBT data.</p>
 */
@ParametersAreNonnullByDefault
public final class NBT {

    public static final int TAG_END = Constants.NBT.TAG_END;
    public static final int TAG_BYTE = Constants.NBT.TAG_BYTE;
    public static final int TAG_SHORT = Constants.NBT.TAG_SHORT;
    public static final int TAG_INT = Constants.NBT.TAG_INT;
    public static final int TAG_LONG = Constants.NBT.TAG_LONG;
    public static final int TAG_FLOAT = Constants.NBT.TAG_FLOAT;
    public static final int TAG_DOUBLE = Constants.NBT.TAG_DOUBLE;
    public static final int TAG_BYTE_ARRAY = Constants.NBT.TAG_BYTE_ARRAY;
    public static final int TAG_STRING = Constants.NBT.TAG_STRING;
    public static final int TAG_LIST = Constants.NBT.TAG_LIST;
    public static final int TAG_COMPOUND = Constants.NBT.TAG_COMPOUND;
    public static final int TAG_INT_ARRAY = Constants.NBT.TAG_INT_ARRAY;

    /**
     * <p>An enumeration of all NBT tag types.</p>
     */
    public static final class Tag<T extends NBTBase> {

        public static final Tag<NBTTagEnd> END = new Tag<>(NBTTagEnd.class, TAG_END, NBTTagEnd::new);
        public static final Tag<NBTTagByte> BYTE = new Tag<>(NBTTagByte.class, TAG_BYTE, () -> new NBTTagByte((byte) 0));
        public static final Tag<NBTTagShort> SHORT = new Tag<>(NBTTagShort.class, TAG_SHORT, NBTTagShort::new);
        public static final Tag<NBTTagInt> INT = new Tag<>(NBTTagInt.class, TAG_INT, () -> new NBTTagInt(0));
        public static final Tag<NBTTagLong> LONG = new Tag<>(NBTTagLong.class, TAG_LONG, () -> new NBTTagLong(0));
        public static final Tag<NBTTagFloat> FLOAT = new Tag<>(NBTTagFloat.class, TAG_FLOAT, () -> new NBTTagFloat(0));
        public static final Tag<NBTTagDouble> DOUBLE = new Tag<>(NBTTagDouble.class, TAG_DOUBLE, () -> new NBTTagDouble(0));
        public static final Tag<NBTTagByteArray> BYTE_ARRAY = new Tag<>(NBTTagByteArray.class, TAG_BYTE_ARRAY, () -> new NBTTagByteArray(ArrayUtils.EMPTY_BYTE_ARRAY));
        public static final Tag<NBTTagString> STRING = new Tag<>(NBTTagString.class, TAG_STRING, NBTTagString::new);
        public static final Tag<NBTTagList> LIST = new Tag<>(NBTTagList.class, TAG_LIST, NBTTagList::new);
        public static final Tag<NBTTagCompound> COMPOUND = new Tag<>(NBTTagCompound.class, TAG_COMPOUND, NBTTagCompound::new);
        public static final Tag<NBTTagIntArray> INT_ARRAY = new Tag<>(NBTTagIntArray.class, TAG_INT_ARRAY, () -> new NBTTagIntArray(ArrayUtils.EMPTY_INT_ARRAY));

        private final Class<T> clazz;
        private final int id;
        final Supplier<T> constructor;

        private Tag(Class<T> clazz, int id, Supplier<T> constructor) {
            this.clazz = clazz;
            this.id = id;
            this.constructor = constructor;
        }

        /**
         * <p>The id for this tag type, as returned by {@link net.minecraft.nbt.NBTBase#getId()}.</p>
         *
         * @return the type id
         */
        public final int id() {
            return id;
        }

        /**
         * <p>Get the tag class corresponding to this tag type (e.g. {@link NBTTagString} for {@code STRING}).</p>
         *
         * @return the class
         */
        public final Class<T> getTagClass() {
            return clazz;
        }

        public boolean isInstance(NBTBase nbt) {
            return nbt.getId() == id;
        }

        public T cast(NBTBase nbt) {
            if (nbt.getId() == id) {
                //noinspection unchecked
                return (T) nbt;
            } else {
                throw new ClassCastException(nbt.getClass() + " cannot be cast to " + clazz);
            }
        }

        /**
         * <p>Get the tag type specified by the given type id.</p>
         *
         * @param id the type id
         * @return the tag type
         */
        public static Tag<?> byId(int id) {
            checkArgument(id >= 0 && id <= 11, "NBT type id out of range");
            return BY_ID[id];
        }

        /**
         * <p>Get the tag type based on the given class.</p>
         *
         * @param clazz the tag class
         * @return the tag type
         */
        public static <T extends NBTBase> Tag<T> byClass(Class<T> clazz) {
            @SuppressWarnings("unchecked")
            Tag<T> t = (Tag<T>) BY_CLAZZ.get(clazz);
            if (t == null) {
                checkNotNull(clazz, "clazz");
                throw new IllegalArgumentException("Invalid NBT Tag class " + clazz.getName());
            }
            return t;
        }

        private static final Tag<?>[] BY_ID;
        private static final ImmutableMap<Class<? extends NBTBase>, Tag<?>> BY_CLAZZ;

        static {
            Tag<?>[] values = {END, BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, BYTE_ARRAY, STRING, LIST, COMPOUND, INT_ARRAY};
            int maxId = 0;
            for (Tag<?> value : values) {
                maxId = Math.max(maxId, value.id);
            }

            BY_ID = new Tag[maxId + 1];

            ImmutableMap.Builder<Class<? extends NBTBase>, Tag<?>> b = ImmutableMap.builder();
            for (Tag<?> tag : values) {
                b.put(tag.clazz, tag);
                BY_ID[tag.id] = tag;
            }
            BY_CLAZZ = b.build();
        }

    }

    /**
     * <p>Get a List view of the given NBTTagList.
     * The returned List is modifiable and writes through to the underlying NBTTagList.</p>
     * <p>The type-parameter {@code T} can be used to narrow the type of the List, if it is known. If a wrong type is
     * provided here, a {@code ClassCastException} may be thrown at any time in the future.</p>
     * <p>Care must also be taken if {@code T} is not provided and values of a wrong type are written into the List.</p>
     *
     * @param nbt the underlying NBTTagList
     * @return a modifiable List view
     */
    @Nonnull
    public static <T extends NBTBase> List<T> asList(NBTTagList nbt) {
        try {
            //noinspection unchecked
            return (List<T>) nbtListGet.invokeExact(nbt);
        } catch (Throwable x) {
            throw Throwables.propagate(x);
        }
    }

    /**
     * <p>Get a Map view of the given NBTTagCompound.
     * The returned Map is modifiable and writes through to the underlying NBTTagCompound.</p>
     * <p>Note that the returned Map does <i>not</i> create default values of any kind, as opposed to NBTTagCompound.</p>
     *
     * @param nbt the underlying NBTTagCompound
     * @return a modifiable Map view
     */
    @Nonnull
    public static Map<String, NBTBase> asMap(NBTTagCompound nbt) {
        try {
            //noinspection unchecked
            return (Map<String, NBTBase>) nbtMapGet.invokeExact(nbt);
        } catch (Throwable x) {
            throw Throwables.propagate(x);
        }
    }

    private static final MethodHandle nbtListGet;
    private static final MethodHandle nbtMapGet;

    static {
        try {
            Field field = NBTTagList.class.getDeclaredField(MCPNames.field(SRGConstants.F_TAG_LIST));
            field.setAccessible(true);
            nbtListGet = publicLookup().unreflectGetter(field);

            field = NBTTagCompound.class.getDeclaredField(MCPNames.field(SRGConstants.F_TAG_MAP));
            field.setAccessible(true);
            nbtMapGet = publicLookup().unreflectGetter(field);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }


    /**
     * <p>Get an NBTTag of the given type or create it if it does not exist.</p>
     *
     * @param parent the parent tag
     * @param key    the key
     * @param tag    the type of NBTTag
     * @return the NBTTag
     */
    public static <T extends NBTBase> T getOrCreate(NBTTagCompound parent, String key, Tag<T> tag) {
        //noinspection unchecked
        return getOrCreate(parent, key, tag, tag.constructor);
    }

    /**
     * <p>Get an NBTTag of the given type or create it if it does not exist.</p>
     *
     * @param parent  the parent tag
     * @param key     the key
     * @param tag     the type of NBTTag
     * @param creator the supplier to call in case the tag is not present
     * @return the NBTTag
     */
    public static <T extends NBTBase> T getOrCreate(NBTTagCompound parent, String key, Tag<T> tag, Supplier<? extends T> creator) {
        NBTBase nbt = parent.getTag(key);
        //noinspection ConstantConditions yes it can be null -_-
        if (nbt == null || !tag.isInstance(nbt)) {
            nbt = creator.get();
            parent.setTag(key, nbt);
        }
        //noinspection unchecked
        return (T) nbt;
    }

    /**
     * <p>Get an NBTTag of the given type or create it if it does not exist.</p>
     *
     * @param parent  the parent tag
     * @param key     the key
     * @param tag     the type of NBTTag
     * @param creator the function to call with the key in case the tag is not present
     * @return the NBTTag
     */
    public static <T extends NBTBase> T getOrCreate(NBTTagCompound parent, String key, Tag<T> tag, Function<? super String, ? extends T> creator) {
        NBTBase nbt = parent.getTag(key);
        //noinspection ConstantConditions yes it can be null...
        if (nbt == null || !tag.isInstance(nbt)) {
            nbt = creator.apply(key);
            parent.setTag(key, nbt);
        }
        //noinspection unchecked
        return (T) nbt;
    }

    /**
     * <p>Get the NBTTagCompound with the given key in {@code parent} or, if no entry for that key is present,
     * create a new NBTTagCompound and store it in {@code parent} with the given key.</p>
     *
     * @param parent the parent NBTTagCompound
     * @param key    the key
     * @return an NBTTagCompound
     */
    @Nonnull
    public static NBTTagCompound getOrCreateCompound(NBTTagCompound parent, String key) {
        return getOrCreate(parent, key, Tag.COMPOUND);
    }

    /**
     * <p>Get the NBTTagList with the given key in {@code parent} of, if no entry for that key is present,
     * create a new NBTTagList and store it in {@code parent} with the given key.</p>
     *
     * @param parent the parent NBTTagCompound
     * @param key    the key
     * @return an NBTTagList
     */
    @Nonnull
    public static NBTTagList getOrCreateList(NBTTagCompound parent, String key) {
        return getOrCreate(parent, key, Tag.LIST);
    }

    /**
     * <p>Create a deep copy of the given NBT-Tag.</p>
     *
     * @param nbt the tag
     * @return a deep copy of the tag
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Contract("null->null")
    public static <T extends NBTBase> T copy(@Nullable T nbt) {
        return nbt == null ? null : (T) nbt.copy();
    }

    private NBT() {
    }

}
