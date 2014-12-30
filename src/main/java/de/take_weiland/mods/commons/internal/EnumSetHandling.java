package de.take_weiland.mods.commons.internal;

/**
 * @author diesieben07
 */
public abstract class EnumSetHandling {
//
//    private static final Unsafe unsafe = JavaUtils.getUnsafe();
//    private static final long typeFieldOff;
//
//    static {
//        Field found = null;
//        for (Field field : EnumSet.class.getDeclaredFields()) {
//            if (!Modifier.isStatic(field.getModifiers()) && field.getType() == Class.class) {
//                found = field;
//                break;
//            }
//        }
//        if (found == null) {
//            throw new RuntimeException("Could not find type field in EnumSet");
//        }
//        typeFieldOff = unsafe.objectFieldOffset(found);
//    }
//
//    public static final String DATA_TAG = "d";
//    public static final String TYPE_TAG = "t";
//
//    public static <E extends Enum<E>> NBTBase toNbt(EnumSet<E> enumSet) {
//        if (enumSet == null) {
//            return NBT.serializedNull();
//        }
//        Class<E> enumClass = getEnumType(enumSet);
//        String typeID = Types.getID(enumClass);
//        NBTTagCompound nbt = new NBTTagCompound();
//        nbt.setString(TYPE_TAG, typeID);
//        if (!enumSet.isEmpty()) {
//            NBTTagList list = new NBTTagList();
//            for (E e : enumSet) {
//                list.appendTag(new NBTTagString("", e.name()));
//            }
//        }
//        return nbt;
//    }
//
//    public static <E extends Enum<E>> EnumSet<E> fromNbt(NBTBase nbt, Class<E> enumClass, EnumSet<E> enumSet) {
//        if (NBT.isSerializedNull(nbt)) {
//            return null;
//        }
//        NBTTagCompound comp = (NBTTagCompound) nbt;
//        if (enumSet == null) {
//            enumSet = EnumSet.noneOf(enumClass);
//        }
//        if (Types.getClass(comp.getString(TYPE_TAG)) != enumClass) {
//            return enumSet;
//        }
//        fromNbt0(enumSet, enumClass, comp.getTagList(DATA_TAG));
//        return enumSet;
//    }
//
//    @SuppressWarnings({"unchecked", "rawtypes"})
//    public static EnumSet<?> fromNbt(NBTBase nbt, EnumSet<?> enumSet) {
//        if (NBT.isSerializedNull(nbt)) {
//            return null;
//        }
//        NBTTagCompound comp = (NBTTagCompound) nbt;
//        String typeID = comp.getString(TYPE_TAG);
//        Class enumClass = Types.getClass(typeID);
//        if (!enumClass.isEnum()) {
//            throw new RuntimeException("TypeID " + typeID + " does not represent an Enum!");
//        }
//        if (enumSet == null || getEnumType(enumSet) != enumClass) {
//            enumSet = EnumSet.noneOf(enumClass);
//        } else {
//            enumSet.clear();
//        }
//       fromNbt0(enumSet, enumClass, comp.getTagList(DATA_TAG));
//        return enumSet;
//    }
//
//    @SuppressWarnings({"rawtypes", "unchecked"})
//    private static void fromNbt0(EnumSet set, Class clazz, NBTTagList nbt) {
//        for (NBTTagString elem : NBT.<NBTTagString>asList(nbt)) {
//            set.add(Enum.valueOf(clazz, elem.data));
//        }
//    }
//
//    public static <E extends Enum<E>> void writeToUnkownType(EnumSet<E> enumSet, MCDataOutputStream out) {
//        if (enumSet == null) {
//            out.writeBoolean(false);
//        } else {
//            out.writeBoolean(true);
//            Class<E> enumType = getEnumType(enumSet);
//            out.writeInt(Types.getNumericalID(enumType));
//            writeToKnownType(enumSet, enumType, out);
//        }
//    }
//
//    public static <E extends Enum<E>> void writeToKnownType(EnumSet<E> enumSet, Class<E> type, MCDataOutputStream out) {
//        if (enumSet == null) {
//            out.writeByte(0b0000_0001);
//        } else {
//            E[] universe = JavaUtils.getEnumConstantsShared(type);
//            int numEnums = universe.length;
//            if (numEnums == 0) {
//                out.writeByte(0);
//            } else {
//                int numBytes = getByteCount(numEnums);
//                int pos = out.length();
//                out.writeNulls(numBytes);
//
//                byte[] arr = out.backingArray();
//
//                for (E e : enumSet) {
//                    int ord = e.ordinal() + 1; // ordinal 0 = set is null
//                    arr[pos + ord >> 3] |= 1 << (ord & 3);
//                }
//            }
//        }
//    }
//
//    @SuppressWarnings({"rawtypes", "unchecked"})
//    public static EnumSet<?> readUnkownType(EnumSet<?> set, MCDataInputStream in) {
//        if (!in.readBoolean()) {
//            return null;
//        }
//        Class clazz = Types.getClass(in.readInt());
//        if (!clazz.isEnum()) {
//            throw new RuntimeException("received non-enum class for EnumSet");
//        }
//        if (set == null || getEnumType(set) != clazz) {
//            set = EnumSet.noneOf(clazz);
//        }
//        return readKnownType(clazz, set, in);
//    }
//
//    public static <E extends Enum<E>> EnumSet<E> readKnownType(Class<E> clazz, EnumSet<E> set, MCDataInputStream in) {
//        byte first = in.readByte();
//        if ((first & 0b1) != 0) {
//            return null;
//        }
//
//        if (set == null) {
//            set = EnumSet.noneOf(clazz);
//        } else {
//            set.clear();
//        }
//        E[] universe = JavaUtils.getEnumConstantsShared(clazz);
//        for (int bit = 1; bit < 8; bit++) {
//            if ((first & (1 << bit)) != 0) {
//                set.add(universe[bit - 1]);
//            }
//        }
//
//        int numBytes = getByteCount(universe.length);
//        for (int i = 1; i < numBytes; i++) {
//            byte b = in.readByte();
//            for (int bit = 0; bit < 8; bit++) {
//                if ((b & (1 << bit)) != 0) {
//                    set.add(universe[(i << 3 - 1) + bit]);
//                }
//            }
//        }
//        return set;
//    }
//
//    private static int getByteCount(int numEnums) {
//        // unoptimized: numBytes = numEnums / 8 + (numEnums % 8 == 0 ? 1 : 0);
//        return numEnums >> 3 + (-(numEnums & 7) >>> 31);
//    }
//
//    @SuppressWarnings("unchecked")
//    public static <E extends Enum<E>> Class<E> getEnumType(EnumSet<E> enumSet) {
//        return (Class<E>) unsafe.getObject(enumSet, typeFieldOff);
//    }

}
