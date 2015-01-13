package de.take_weiland.mods.commons.nbt;

import com.google.common.collect.Maps;
import de.take_weiland.mods.commons.serialize.NBTSerializer;
import de.take_weiland.mods.commons.util.JavaUtils;
import de.take_weiland.mods.commons.util.SCReflector;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * <p>Utility methods regarding NBT data.</p>
 * <p>The following types are supported for serialization:</p>
 * <ul>
 *     <li>{@link java.lang.String}</li>
 *     <li>{@link java.util.UUID}</li>
 *     <li>{@link net.minecraft.item.ItemStack}</li>
 *     <li>{@link net.minecraftforge.fluids.FluidStack}</li>
 *     <li>Any subtype of {@link java.lang.Enum}</li>
 *     <li>Any subtype of {@link de.take_weiland.mods.commons.nbt.NBTSerializable}</li>
 *     <li>Any type with a custom serializer, see {@link #registerSerializer(Class, NBTSerializer)}</li>
 *     <li>Any primitive array (TODO: multi-dim)</li>
 *     <li>Any array of one of the above types (TODO: multi-dim)</li>
 * </ul>
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

		END,
		BYTE,
		SHORT,
		INT,
		LONG,
		FLOAT,
		DOUBLE,
		BYTE_ARRAY,
		STRING,
		LIST,
		COMPOUND,
		INT_ARRAY;

		/**
		 * <p>The id for this tag type, as returned by {@link net.minecraft.nbt.NBTBase#getId()}.</p>
		 * @return the type id
		 */
		public final int id() {
			return ordinal();
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

		private static final Tag[] VALUES = values();

	}

	/**
	 * <p>Create a null-safe version of the given serializer.</p>
	 * @param serializer the serializer to wrap
	 * @return a null-safe version of the serializer
	 */
	public static <T> NBTSerializer<T> makeNullSafe(NBTSerializer<T> serializer) {
		return NullSafeSerializerWrapper.makeNullSafe(serializer);
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
