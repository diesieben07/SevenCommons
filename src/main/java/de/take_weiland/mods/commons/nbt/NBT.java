package de.take_weiland.mods.commons.nbt;

import com.google.common.collect.Maps;
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
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

	private static Map<String, NBTSerializer.NullSafe<?>> serializers = Maps.newHashMap();

	/**
	 * <p>Create a null-safe version of the given serializer.</p>
	 * @param serializer the serializer to wrap
	 * @return a null-safe version of the serializer
	 */
	public static <T> NBTSerializer.NullSafe<T> makeNullSafe(NBTSerializer<T> serializer) {
		if (serializer instanceof NBTSerializer.NullSafe) {
			return (NBTSerializer.NullSafe<T>) serializer;
		} else {
			return new NullSafeSerializerWrapper<>(serializer);
		}
	}

	private static final byte NULL = -1;
	private static final String NULL_KEY = "_sc$null";

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
	 * The returned Map is modifiable and writes through to teh underlying NBTTagCompound.</p>
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

	@SuppressWarnings("unchecked")
	public static <T> NBTSerializer.NullSafe<T> getSerializer(String typeID) {
		NBTSerializer.NullSafe<?> serializer = serializers.get(typeID);
		if (serializer == null) {
			throw new IllegalArgumentException("No NBTSerializer for typeID " + typeID);
		}
		return (NBTSerializer.NullSafe<T>) serializer;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static <T> NBTSerializer.NullSafe<T> newNullSafeSerializer(final Class<T> clazz) {
		if (clazz.isEnum()) {
			return (NBTSerializer.NullSafe<T>) new EnumSerializer(clazz);
		} else if (clazz == String.class) {
			return (NBTSerializer.NullSafe<T>) new StringSerializer();
		} else if (clazz == UUID.class) {
			return (NBTSerializer.NullSafe<T>) new UUIDSerializer();
		} else if (clazz == ItemStack.class) {
			return (NBTSerializer.NullSafe<T>) new ItemStackSerializer();
		} else if (clazz == FluidStack.class) {
			return (NBTSerializer.NullSafe<T>) new FluidStackSerializer();
		} else if (clazz == boolean[].class) {
			return (NBTSerializer.NullSafe<T>) new PrimitiveArraySerializers.Boolean();
		} else if (clazz == byte[].class) {
			return (NBTSerializer.NullSafe<T>) new PrimitiveArraySerializers.Byte();
		} else if (clazz == short[].class) {
			return (NBTSerializer.NullSafe<T>) new PrimitiveArraySerializers.Short();
		} else if (clazz == char[].class) {
			return (NBTSerializer.NullSafe<T>) new PrimitiveArraySerializers.Char();
		} else if (clazz == int[].class) {
			return (NBTSerializer.NullSafe<T>) new PrimitiveArraySerializers.Int();
		} else if (clazz == long[].class) {
			return (NBTSerializer.NullSafe<T>) new PrimitiveArraySerializers.Long();
		} else if (clazz == float[].class) {
			return (NBTSerializer.NullSafe<T>) new PrimitiveArraySerializers.Float();
		} else if (clazz == double[].class) {
			return (NBTSerializer.NullSafe<T>) new PrimitiveArraySerializers.Double();
		} else if (clazz == String[].class) {
			return (NBTSerializer.NullSafe<T>) new ObjectArraySerializer.StringSpecialized();
		} else if (clazz == ItemStack[].class) {
			return (NBTSerializer.NullSafe<T>) new ObjectArraySerializer.ItemStackSpecialized();
		} else if (clazz.isArray() && JavaUtils.getDimensions(clazz) == 1) {
			Class<?> comp = clazz.getComponentType();
//			return (NBTSerializer.NullSafe<T>) new ObjectArraySerializer.Simple<>(getSerializer(comp), comp);
		}
//		final NBTSerializer<T> wrapped = (NBTSerializer<T>) customSerializers.get(clazz);
//		if (wrapped == null) {
//			throw cannotSerialize(clazz);
//		} else {
//			return new NullSafeSerializerWrapper<>(wrapped);
//		}
		return null;
	}

	private static RuntimeException cannotSerialize(Class<?> clazz) {
		return new RuntimeException("Cannot serialize " + clazz.getName() + " to NBT");
	}

	/**
	 * <p>Write the given String to NBT.</p>
	 * @param s the String
	 * @return NBT data
	 */
	@Nonnull
	public static NBTBase writeString(@Nullable String s) {
		return s == null ? serializedNull() : new NBTTagString("", s);
	}

	/**
	 * <p>Stores the given String to the given key in the NBTTagCompound.</p>
	 * @param s the String
	 * @param nbt the NBTTagCompound
	 * @param key the key
	 */
	public static void writeString(@Nullable String s, NBTTagCompound nbt, String key) {
		nbt.setTag(key, writeString(s));
	}

	/**
	 * <p>Read a String from NBT.</p>
	 * @param nbt the NBT data
	 * @return a String
	 */
	@Nullable
	public static String readString(@Nullable NBTBase nbt) {
		return isSerializedNull(nbt) ? null : ((NBTTagString) nbt).data;
	}

	/**
	 * <p>Read a String from the given key in the NBTTagCompound.</p>
	 * @param nbt the NBTTagCompound
	 * @param key the key
	 * @return a String
	 */
	@Nullable
	public static String readString(NBTTagCompound nbt, String key) {
		return readString(nbt.getTag(key));
	}

	@Nonnull
	public static NBTBase writeBlock(@Nullable Block block) {
		if (block == null) {
			return serializedNull();
		} else {
			return new NBTTagShort("", (short) block.blockID);
		}
	}

	public static void writeBlock(@Nullable Block block, NBTTagCompound nbt, String key) {
		nbt.setTag(key, writeBlock(block));
	}

	@Nullable
	public static Block readBlock(@Nullable NBTBase nbt) {
		if (isSerializedNull(nbt)) {
			return null;
		} else {
			return Block.blocksList[((NBTTagShort) nbt).data];
		}
	}

	@Nullable
	public static Block readBlock(NBTTagCompound nbt, String key) {
		return readBlock(nbt.getTag(key));
	}

	@Nonnull
	public static NBTBase writeItem(@Nullable Item item) {
		if (item == null) {
			return serializedNull();
		} else {
			return new NBTTagShort("", (short) item.itemID);
		}
	}

	public static void writeItem(@Nullable Item item, NBTTagCompound nbt, String key) {
		nbt.setTag(key, writeItem(item));
	}

	@Nullable
	public static Item readItem(@Nullable NBTBase nbt) {
		if (isSerializedNull(nbt)) {
			return null;
		} else {
			return Item.itemsList[((NBTTagShort) nbt).data];
		}
	}

	@Nullable
	public static Item readItem(NBTTagCompound nbt, String key) {
		return readItem(nbt.getTag(key));
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
			nbt.appendTag(new NBTTagLong("", uuid.getMostSignificantBits()));
			nbt.appendTag(new NBTTagLong("", uuid.getLeastSignificantBits()));
			return nbt;
		}
	}

	/**
	 * <p>Stores the given UUID to the given key in the NBTTagCompound.</p>
	 * @param uuid the UUID
	 * @param nbt the NBTTagCompound
	 * @param key the key
	 */
	public static void writeUUID(@Nullable UUID uuid, NBTTagCompound nbt, String key) {
		nbt.setTag(key, writeUUID(uuid));
	}

	/**
	 * <p>Read an UUID from NBT.</p>
	 * @param nbt the NBT data
	 * @return an UUID
	 */
	@Nullable
	public static UUID readUUID(@Nullable NBTBase nbt) {
		if (isSerializedNull(nbt) || nbt.getId() != TAG_LIST) {
			return null;
		} else {
			NBTTagList list = (NBTTagList) nbt;
			return new UUID(((NBTTagLong) list.tagAt(0)).data, ((NBTTagLong) list.tagAt(1)).data);
		}
	}

	/**
	 * <p>Read an UUID from the given key in the NBTTagCompound.</p>
	 * @param nbt the NBTTagCompound
	 * @param key the key
	 * @return an UUID
	 */
	@Nullable
	public static UUID readUUID(NBTTagCompound nbt, String key) {
		return readUUID(nbt.getTag(key));
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
	 * <p>Stores the given ItemStack to the given key in the NBTTagCompound.</p>
	 * @param stack the ItemStack
	 * @param nbt the NBTTagCompound
	 * @param key the key
	 */
	public static void writeItemStack(@Nullable ItemStack stack, NBTTagCompound nbt, String key) {
		nbt.setTag(key, writeItemStack(stack));
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
	 * <p>Read an ItemStack from the given key in the NBTTagCompound.</p>
	 * @param nbt the NBTTagCompound
	 * @param key the key
	 * @return an ItemStack
	 */
	@Nullable
	public static ItemStack readItemStack(NBTTagCompound nbt, String key) {
		return readItemStack(nbt.getTag(key));
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
	 * <p>Stores the given FluidStack to the given key in the NBTTagCompound.</p>
	 * @param stack the FluidStack
	 * @param nbt the NBTTagCompound
	 * @param key the key
	 */
	public static void writeFluidStack(@Nullable FluidStack stack, NBTTagCompound nbt, String key) {
		nbt.setTag(key, writeFluidStack(stack));
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
	 * <p>Read a FluidStack from the given key in the NBTTagCompound.</p>
	 * @param nbt the NBTTagCompound
	 * @param key the key
	 * @return a FluidStack
	 */
	@Nullable
	public static FluidStack readFluidStack(NBTTagCompound nbt, String key) {
		return readFluidStack(nbt.getTag(key));
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
			return new NBTTagString("", e.name());
		}
	}

	/**
	 * <p>Stores the given Enum to the given key in the NBTTagCompound.</p>
	 * @param e the Enum
	 * @param nbt the NBTTagCompound
	 * @param key the key
	 */
	public static <E extends Enum<E>> void writeEnum(NBTTagCompound nbt, String key, @Nullable E e) {
		nbt.setTag(key, writeEnum(e));
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
			return Enum.valueOf(clazz, ((NBTTagString) nbt).data);
		}
	}

	/**
	 * <p>Read an Enum from the given key in the NBTTagCompound.</p>
	 * @param nbt the NBTTagCompound
	 * @param key the key
	 * @param clazz the Class of the Enum to read
	 * @return an Enum
	 */
	@Nullable
	public static <E extends Enum<E>> E readEnum(NBTTagCompound nbt, String key, Class<E> clazz) {
		return readEnum(nbt.getTag(key), clazz);
	}

	@Nonnull
	public static <E extends Enum<E>> NBTBase writeEnumSet(@Nullable EnumSet<E> enumSet) {
		if (enumSet == null) {
			return serializedNull();
		} else {
			NBTTagList list = new NBTTagList();
			for (E e : enumSet) {
				list.appendTag(new NBTTagString("", e.name()));
			}
			return list;
		}
	}

	public static <E extends Enum<E>> void writeEnumSet(@Nullable EnumSet<E> enumSet, NBTTagCompound nbt, String key) {
		nbt.setTag(key, writeEnumSet(enumSet));
	}

	@Nullable
	public static <E extends Enum<E>> EnumSet<E> readEnumSet(@Nullable NBTBase nbt, Class<E> enumClass) {
		if (isSerializedNull(nbt)) {
			return null;
		} else {
			EnumSet<E> enumSet = EnumSet.noneOf(enumClass);
			NBTTagList list = (NBTTagList) nbt;
			for (int i = 0, len = list.tagCount(); i < len; i++) {
				enumSet.add(Enum.valueOf(enumClass, ((NBTTagString) list.tagAt(i)).data));
			}
			return enumSet;
		}
	}

	@Nullable
	public static <E extends Enum<E>> EnumSet<E> readEnumSet(NBTTagCompound nbt, String key, Class<E> enumClass) {
		return readEnumSet(nbt.getTag(key), enumClass);
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
	 * <p>Check if the given NBT Tag represents a serialized {@code null} reference.</p>
	 * @param nbt the NBT data
	 * @return true if the NBT data represents null
	 * @see #serializedNull()
	 */
	@Contract("null->true")
	public static boolean isSerializedNull(@Nullable NBTBase nbt) {
		return nbt == null || (nbt.getId() == TAG_COMPOUND && ((NBTTagCompound) nbt).getByte(NULL_KEY) == NULL);
	}

	private NBT() { }

}
