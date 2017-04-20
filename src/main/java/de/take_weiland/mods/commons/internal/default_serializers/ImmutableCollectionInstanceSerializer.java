package de.take_weiland.mods.commons.internal.default_serializers;

/**
 * @author diesieben07
 */
//final class ImmutableCollectionInstanceSerializer<T, C extends ImmutableCollection<T>, B extends ImmutableCollection.Builder<T>> extends CollectionInstanceSerializer<T, C> {
//
//    private final Supplier<B> builderSupplier;
//
//    private ImmutableCollectionInstanceSerializer(NBTSerializer.Value<T> elementSerializer, Supplier<B> builderSupplier) {
//        super(elementSerializer);
//        this.builderSupplier = builderSupplier;
//    }
//
//    @Override
//    public int getInputNbtId() {
//        return NBT.TAG_LIST;
//    }
//
//    @Override
//    public C read(NBTBase nbt) throws SerializationException {
//        B builder = builderSupplier.get();
//        NBTTagList list = (NBTTagList) nbt;
//        int size = list.tagCount();
//
//        for (int i = 0; i < size; i++) {
//            NBTBase element = list.get(i);
//            if (NBTData.idMatches(element, elementSerializer.getInputNbtId())) {
//                builder.add(elementSerializer.read(element));
//            }
//        }
//
//        //noinspection unchecked
//        return (C) builder.build();
//    }
//
//    static <T, C extends ImmutableCollection<C>> NBTSerializer.Value<C> create(SerializerRegistry registry, TypeToken<C> type, NBTSerializer.Value<T> elementSerializer) {
//        Class<?> raw = type.getRawType();
//        if (ContiguousSet.class.isAssignableFrom(raw)) {
//
//        } else if (ImmutableSortedSet.class.isAssignableFrom(raw)) {
//            // TODO
//            return new SortedSet_(registry, elementSerializer);
//        } else if (ImmutableSet.class.isAssignableFrom(raw)) {
//            return new ImmutableCollectionInstanceSerializer(elementSerializer, ImmutableSet::builder);
//        } else if (ImmutableList.class.isAssignableFrom(raw)) {
//            return new ImmutableCollectionInstanceSerializer(elementSerializer, ImmutableList::builder);
//        } else if (ImmutableMultiset.class.isAssignableFrom(raw)) {
//            return new Multiset_(elementSerializer);
//        }
//    }
//
//    private static final class SortedSet_<T> implements NBTSerializer.Value<ImmutableSortedSet<T>> {
//
//        private final SerializerRegistry registry;
//        private final Value<T> elementSerializer;
//
//        SortedSet_(SerializerRegistry registry, Value<T> elementSerializer) {
//            this.elementSerializer = elementSerializer;
//            this.registry = registry;
//        }
//
//
//        @Override
//        public ImmutableSortedSet<T> read(NBTBase nbt) throws SerializationException {
//            NBTTagCompound c = (NBTTagCompound) nbt;
//            NBTBase cSer = c.getTag("c");
//            if (cSer != null) {
//                return registry.readNbtInstance()
//            }
//            return null;
//        }
//
//        @Override
//        public NBTBase write(ImmutableSortedSet<T> value) throws SerializationException {
//            NBTTagCompound nbt = new NBTTagCompound();
//            Comparator<?> c = value.comparator();
//            if (c != null && c != Ordering.natural() && c != Comparator.naturalOrder()) {
//                NBTBase cSer = registry.writeNbtInstance(c, false);
//                nbt.setTag("c", cSer);
//            }
//            NBTTagList list = new NBTTagList();
//            for (T t : value) {
//                list.appendTag(elementSerializer.write(t));
//            }
//            nbt.setTag("l", list);
//            return nbt;
//        }
//    }
//
//    private static final class Multiset_<T> implements NBTSerializer.Value<ImmutableMultiset<T>> {
//
//        private final Value<T> elementSerializer;
//
//        Multiset_(Value<T> elementSerializer) {
//            this.elementSerializer = elementSerializer;
//        }
//
//        @Override
//        public int getInputNbtId() {
//            return NBT.TAG_LIST;
//        }
//
//        @Override
//        public ImmutableMultiset<T> read(NBTBase nbt) throws SerializationException {
//            NBTTagList list = (NBTTagList) nbt;
//            ImmutableMultiset.Builder<T> builder = ImmutableMultiset.builder();
//            for (int i = 0, n = list.tagCount(); i < n; i++) {
//                NBTTagCompound c = list.getCompoundTagAt(i);
//                builder.addCopies(elementSerializer.read(c.getTag("e")), c.getInteger("c"));
//            }
//            return builder.build();
//        }
//
//        @Override
//        public NBTBase write(ImmutableMultiset<T> value) throws SerializationException {
//            NBTTagList list = new NBTTagList();
//            for (Multiset.Entry<T> entry : value.entrySet()) {
//                NBTTagCompound c = new NBTTagCompound();
//                c.setTag("e", elementSerializer.write(entry.getElement()));
//                c.setInteger("c", entry.getCount());
//                list.appendTag(c);
//            }
//            return list;
//        }
//    }
//
//    static <T> ImmutableCollectionInstanceSerializer<T, ImmutableList<T>, ImmutableList.Builder<T>> list(NBTSerializer.Value<T> elementSerializer) {
//        return new ImmutableCollectionInstanceSerializer<>(elementSerializer, ImmutableList::builder);
//    }
//
//    static <T> ImmutableCollectionInstanceSerializer<T, ImmutableSet<T>, ImmutableSet.Builder<T>> set(NBTSerializer.Value<T> elementSerializer) {
//        return new ImmutableCollectionInstanceSerializer<>(elementSerializer, ImmutableSet::builder);
//    }
//
//}
