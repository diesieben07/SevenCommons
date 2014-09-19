package de.take_weiland.mods.commons.sync;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.util.ByteStreamSerializable;
import de.take_weiland.mods.commons.util.ByteStreamSerializer;
import de.take_weiland.mods.commons.util.Serializers;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author diesieben07
 */
public abstract class SyncedSet<E> implements Set<E> {

	/**
	 * <p>Create a new {@code SyncedSet} for holding ItemStacks, based on an empty {@link java.util.HashSet}.
	 * The returned Set supports null values.</p>
	 * @return a new, empty SyncedSet
	 */
	public static SyncedSet<ItemStack> forItemStack() {
		return forItemStack(new HashSet<ItemStack>());
	}

	/**
	 * <p>Create a new {@code SyncedSet} for holding ItemStacks, based the given Set.
	 * The returned Set supports null values if the delegate does.</p>
	 * <p>It is recommended that the passed in Set is empty.</p>
	 * @param delegate the underlying Set to use
	 * @return a new SyncedSet
	 */
	public static SyncedSet<ItemStack> forItemStack(@NotNull Set<ItemStack> delegate) {
		return new WithSerializer<>(delegate, Serializers.forItemStack());
	}

	/**
	 * <p>Create a new {@code SyncedSet} for holding FluidStacks, based on an empty {@link java.util.HashSet}.
	 * The returned Set supports null values.</p>
	 * @return a new SyncedSet
	 */
	public static SyncedSet<FluidStack> forFluidStack() {
		return forFluidStack(new HashSet<FluidStack>());
	}

	/**
	 * <p>Create a new {@code SyncedSet} for holding FluidStacks, based the given Set.
	 * The returned Set supports null values if the delegate does.</p>
	 * <p>It is recommended that the passed in Set is empty.</p>
	 * @param delegate the underlying Set
	 * @return a new SyncedSet
	 */
	public static SyncedSet<FluidStack> forFluidStack(@NotNull Set<FluidStack> delegate) {
		return new WithSerializer<>(delegate, Serializers.forFluidStack());
	}

	/**
	 * <p>Create a new {@code SyncedSet} for holding Strings, based on an empty {@link java.util.HashSet}.
	 * The returned Set supports null values.</p>
	 * @return a new SyncedSet
	 */
	public static SyncedSet<String> forString() {
		return forString(new HashSet<String>());
	}

	/**
	 * <p>Create a new {@code SyncedSet} for holding Strings, based the given Set.
	 * The returned Set supports null values if the delegate does.</p>
	 * <p>It is recommended that the passed in Set is empty.</p>
	 * @param delegate the underlying Set
	 * @return a new SyncedSet
	 */
	public static SyncedSet<String> forString(@NotNull Set<String> delegate) {
		return new WithSerializer<>(delegate, Serializers.forString());
	}

	/**
	 * <p>Create a new {@code SyncedSet} for holding UUIDs, based on an empty {@link java.util.HashSet}.
	 * The returned Set supports null values.</p>
	 * @return a new SyncedSet
	 */
	public static SyncedSet<UUID> forUUID() {
		return forUUID(new HashSet<UUID>());
	}

	/**
	 * <p>Create a new {@code SyncedSet} for holding UUIDs, based the given Set.
	 * The returned Set supports null values if the delegate does.</p>
	 * <p>It is recommended that the passed in Set is empty.</p>
	 * @param delegate the underlying Set
	 * @return a new SyncedSet
	 */
	public static SyncedSet<UUID> forUUID(@NotNull Set<UUID> delegate) {
		return new WithSerializer<>(delegate, Serializers.forUUID());
	}

	/**
	 * <p>Create a new {@code SyncedSet} for holding instances of the given class, based on an empty {@link java.util.HashSet}.
	 * The returned Set supports null values.</p>
	 * @param clazz the class of the elements in the Set
	 * @return a new SyncedSet
	 */
	public static <E extends ByteStreamSerializable> SyncedSet<E> create(@NotNull Class<E> clazz) {
		return create(new HashSet<E>(), clazz, true);
	}

	/**
	 * <p>Create a new {@code SyncedSet} for holding instances of the given class, based on an empty {@link java.util.HashSet}.</p>
	 * @param clazz the class of the elements in this Set
	 * @param supportNull if the returned Set should support null values
	 * @return a new SyncedSet
	 */
	public static <E extends ByteStreamSerializable> SyncedSet<E> create(@NotNull Class<E> clazz, boolean supportNull) {
		return create(new HashSet<E>(), clazz, supportNull);
	}

	/**
	 * <p>Create a new {@code SyncedSet} for holding instances of the given class, based on the given Set.
	 * The returned Set supports null values if the delegate does.</p>
	 * <p>It is recommended that the passed in Set is empty.</p>
	 * @param delegate the underlying Set
	 * @param clazz the class of the elements in the Set
	 * @return a new SyncedSet
	 */
	public static <E extends ByteStreamSerializable> SyncedSet<E> create(@NotNull HashSet<E> delegate, @NotNull Class<E> clazz) {
		return create(delegate, clazz, true);
	}

	/**
	 * <p>Create a new {@code SyncedSet} for holding instances of the given class, based on the given Set.
	 * The returned Set supports null values if the delegate does and {@code supportNull} is true.</p>
	 * <p>It is recommended that the passed in Set is empty.</p>
	 * @param delegate the underlying Set
	 * @param clazz the class of the elements in the Set
	 * @param supportNull if the returned Set should support null values
	 * @return a new SyncedSet
	 */
	public static <E extends ByteStreamSerializable> SyncedSet<E> create(@NotNull HashSet<E> delegate, @NotNull Class<E> clazz, boolean supportNull) {
		return new WithSerializer<>(delegate, Serializers.wrap(clazz, supportNull));
	}

	/**
	 * <p>Create a new {@code SyncedSet} for holding instances of class E, based on an empty {@link java.util.HashSet}.
	 * The returned Set supports null values if the serializer does.</p>
	 * @param serializer a ByteStreamSerializer for serializing the elements
	 * @return a new SyncedSet
	 */
	public static <E> SyncedSet<E> withSerializer(@NotNull ByteStreamSerializer<E> serializer) {
		return withSerializer(new HashSet<E>(), serializer);
	}

	/**
	 * <p>Create a new {@code SyncedSet} for holding instances of class E, based on the given Set.
	 * The returned Set supports null values if the serializer and the delegate do.</p>
	 * <p>It is recommended that the passed in Set is empty.</p>
	 * @param delegate the underlying Set
	 * @param serializer a ByteStreamSerializer for serializing the elements
	 * @return a new SyncedSet
	 */
	public static <E> SyncedSet<E> withSerializer(@NotNull HashSet<E> delegate, @NotNull ByteStreamSerializer<E> serializer) {
		return new WithSerializer<>(delegate, serializer);
	}

	/**
	 * <p>Mark this Set as dirty, meaning it will synchronize on next query.</p>
	 */
	public void markDirty() {
		dirty = true;
	}

	// Implementation

	private final Set<E> delegate;
	boolean dirty;

	SyncedSet(Set<E> delegate) {
		this.delegate = delegate;
	}

	/**
		 * <p>If this object's state has changed since the last call to {@link #writeSyncDataAndReset(de.take_weiland.mods.commons.net.MCDataOutputStream)}
		 * in a way that requires re-syncing it to the client.</p>
		 * @return true if this object needs re-syncing
		 */
	public boolean needsSyncing() {
		return dirty;
	}

	/**
		 * <p>Write this object's state to the stream. The method {@link #needsSyncing()} should return false if it's called
		 * immediately after this method and then continue to return false until this object's state changes
		 * in a way that requires re-syncing.</p>
		 * <p>The format of the data is not specified and object-specific. You can do delta updates or
		 * full updates as needed.</p>
		 * <p>The stream passed in must only be written to. <i>If</i> it's position is modified, this method must
		 * make sure that the position points to the next byte after the data written by this method before it returns.</p>
		 * @param out the stream to write your data to
		 */
	public void writeSyncDataAndReset(MCDataOutputStream out) {
		int len = delegate.size();
		out.writeVarInt(len);
		for (E e : delegate) {
			writeElement(e, out);
		}

		dirty = false;
	}

	/**
		 * <p>Read this object's state from the stream.</p>
		 * <p>The stream contains the data in the same way it was written by {@link #writeSyncDataAndReset(de.take_weiland.mods.commons.net.MCDataOutputStream)}.</p>
		 * @param in the stream to read your data from
		 */
	public void readSyncData(MCDataInputStream in) {
		Set<E> delegate = this.delegate;

		delegate.clear();
		int len = in.readVarInt();
		for (int i = 0; i < len; i++) {
			delegate.add(readElement(in));
		}
	}

	abstract void writeElement(E e, MCDataOutputStream out);
	abstract E readElement(MCDataInputStream in);

	@Override
	public boolean add(E e) {
		if (delegate.add(e)) {
			dirty = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean remove(Object o) {
		if (delegate.remove(o)) {
			dirty = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean addAll(@NotNull Collection<? extends E> c) {
		if (delegate.addAll(c)) {
			dirty = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		if (delegate.retainAll(c)) {
			dirty = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		if (delegate.removeAll(c)) {
			dirty = true;
			return true;
		}
		return false;
	}

	@Override
	public void clear() {
		if (!delegate.isEmpty()) {
			delegate.clear();
			dirty = true;
		}
	}

	@NotNull
	@Override
	public Iterator<E> iterator() {
		return new Itr(delegate.iterator());
	}

	private class Itr implements Iterator<E> {

		private final Iterator<E> delegate;

		Itr(Iterator<E> delegate) {
			this.delegate = delegate;
		}

		@Override
		public boolean hasNext() {
			return delegate.hasNext();
		}

		@Override
		public E next() {
			return delegate.next();
		}

		@Override
		public void remove() {
			delegate.remove();
			dirty = true;
		}
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return delegate.contains(o);
	}

	@NotNull
	@Override
	public Object[] toArray() {
		return delegate.toArray();
	}

	@SuppressWarnings("SuspiciousToArrayCall")
	@NotNull
	@Override
	public <T> T[] toArray(@NotNull T[] a) {
		return delegate.toArray(a);
	}

	@Override
	public boolean containsAll(@NotNull Collection<?> c) {
		return delegate.containsAll(c);
	}

	private static class WithSerializer<E> extends SyncedSet<E> {

		private final ByteStreamSerializer<E> serializer;

		WithSerializer(Set<E> delegate, ByteStreamSerializer<E> serializer) {
			super(delegate);
			this.serializer = serializer;
		}

		@Override
		void writeElement(E e, MCDataOutputStream out) {
			serializer.write(e, out);
		}

		@Override
		E readElement(MCDataInputStream in) {
			return serializer.read(in);
		}
	}
}
