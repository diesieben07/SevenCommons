package de.take_weiland.mods.commons.nbt;

import com.google.common.collect.ImmutableMap;
import de.take_weiland.mods.commons.util.SCReflector;
import net.minecraft.nbt.*;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>Utility methods regarding NBT data.</p>
 */
@ParametersAreNonnullByDefault
public final class NBT {

	public static final int TAG_END = 0;
	public static final int TAG_BYTE = 1;
	public static final int TAG_SHORT = 2;
	public static final int TAG_INT = 3;
	public static final int TAG_LONG = 4;
	public static final int TAG_FLOAT = 5;
	public static final int TAG_DOUBLE = 6;
	public static final int TAG_BYTE_ARR = 7;
	public static final int TAG_STRING = 8;
	public static final int TAG_LIST = 9;
	public static final int TAG_COMPOUND = 10;
	public static final int TAG_INT_ARR = 11;

	/**
	 * <p>An enumeration of all NBT tag types.</p>
	 */
	public enum Tag {

		END(NBTTagEnd.class),
		BYTE(NBTTagByte.class),
		SHORT(NBTTagShort.class),
		INT(NBTTagInt.class),
		LONG(NBTTagLong.class),
		FLOAT(NBTTagFloat.class),
		DOUBLE(NBTTagDouble.class),
		BYTE_ARRAY(NBTTagByteArray.class),
		STRING(NBTTagString.class),
		LIST(NBTTagList.class),
		COMPOUND(NBTTagCompound.class),
		INT_ARRAY(NBTTagIntArray.class);

        private final Class<? extends NBTBase> clazz;

        private Tag(Class<? extends NBTBase> clazz) {
            this.clazz = clazz;
        }

        /**
		 * <p>The id for this tag type, as returned by {@link net.minecraft.nbt.NBTBase#getId()}.</p>
		 * @return the type id
		 */
		public final int id() {
			return ordinal();
		}

        /**
         * <p>Get the tag class corresponding to this tag type.</p>
         * @return the class
         */
        public final Class<? extends NBTBase> getTagClass() {
            return clazz;
        }

		/**
		 * <p>Get the tag type specified by the given type id.</p>
		 * @param id the type id
		 * @return the tag type
		 */
		public static Tag byId(int id) {
			checkArgument(id >= 0 && id <= 11, "NBT type id out of range");
			return VALUES[id];
		}

        /**
         * <p>Get the tag type based on the given class.</p>
         * @param clazz the tag class
         * @return the tag type
         */
        public static Tag byClass(Class<?> clazz) {
            Tag t = BY_CLAZZ.get(clazz);
            if (t == null) {
                checkNotNull(clazz, "clazz");
                throw new IllegalArgumentException("Invalid NBT Tag class " + clazz.getName());
            }
            return t;
        }

		private static final Tag[] VALUES = values();
        private static final ImmutableMap<Class<?>, Tag> BY_CLAZZ;

        static {
            ImmutableMap.Builder<Class<?>, Tag> b = ImmutableMap.builder();
            for (Tag tag : VALUES) {
                b.put(tag.clazz, tag);
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
		return SCReflector.instance.getWrappedList(nbt);
	}

	/**
	 * <p>Get a Map view of the given NBTTagCompound.
	 * The returned Map is modifiable and writes through to the underlying NBTTagCompound.</p>
	 * <p>Note that the returned Map does <i>not</i> create default values of any kind, as opposed to NBTTagCompound.</p>
	 * @param nbt the underlying NBTTagCompound
	 * @return a modifiable Map view
	 */
	@Nonnull
	public static Map<String, NBTBase> asMap(NBTTagCompound nbt) {
		return SCReflector.instance.getWrappedMap(nbt);
	}

	/**
	 * <p>Get the NBTTagCompound with the given key in {@code parent} or, if no entry for that key is present,
	 * create a new NBTTagCompound and store it in {@code parent} with the given key.</p>
	 * @param parent the parent NBTTagCompound
	 * @param key the key
	 * @return an NBTTagCompound
	 */
	@Nonnull
	public static NBTTagCompound getOrCreateCompound(NBTTagCompound parent, String key) {
		NBTTagCompound nbt = (NBTTagCompound) asMap(parent).get(key);
		if (nbt == null) {
			parent.setCompoundTag(key, (nbt = new NBTTagCompound()));
		}
		return nbt;
	}

	/**
	 * <p>Get the NBTTagList with the given key in {@code parent} of, if no entry for that key is present,
	 * create a new NBTTagList and store it in {@code parent} with the given key.</p>
	 * @param parent the parent NBTTagCompound
	 * @param key the key
	 * @return an NBTTagList
	 */
	@Nonnull
	public static NBTTagList getOrCreateList(NBTTagCompound parent, String key) {
		NBTTagList list = (NBTTagList) asMap(parent).get(key);
		if (list == null) {
			parent.setTag(key, (list = new NBTTagList()));
		}
		return list;
	}

	/**
	 * <p>Create a deep copy of the given NBT-Tag.</p>
	 * @param nbt the tag
	 * @return a deep copy of the tag
	 */
	@SuppressWarnings("unchecked")
	@Nullable
	@Contract("null->null")
	public static <T extends NBTBase> T copy(@Nullable T nbt) {
		return nbt == null ? null : (T) nbt.copy();
	}

	private NBT() { }

}
