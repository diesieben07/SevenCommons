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

import static com.google.common.base.Preconditions.checkElementIndex;

/**
 * <p>A List implementation which can be used with the {@code @Sync} annotation and is based on an underlying List.</p>
 * <p>As long as the list size is not changed, only the changed indices will be synced to the client.</p>
 * <p>An index is marked for update whenever either {@link #set(int, Object)} or {@link java.util.ListIterator#set(Object)}
 * is called for that index. Additionally an index can be marked as "dirty" manually by calling {@link #markDirty(int)}.</p>
 * <p>Whenever the list size changes, a full update is sent. This can be triggered manually via {@link #markDirty()}.</p>
 * <p>This implementation does not check for changes of it's elements! If the objects in the list are mutable, changes
 * must be reported manually via the methods explained above.</p>
 * <p><strong>Note:</strong> It is highly recommended that the underlying list implements {@link java.util.RandomAccess}
 * for best performance results.</p>
 *
 * @author diesieben07
 */
public abstract class SyncedList<E> implements List<E>, Syncable {

	/**
	 * <p>Create a new {@code SyncedList} for holding ItemStacks, based on an empty {@link java.util.ArrayList}.</p>
	 * @return a new, empty SyncedList
	 */
	public static SyncedList<ItemStack> forItemStack() {
		return forItemStack(new ArrayList<ItemStack>());
	}

	/**
	 * <p>Create a new {@code SyncedList} for holding ItemStacks, based the given List.</p>
	 * <p>It is recommended that the passed in list is empty.</p>
	 * @param delegate the underlying list to use
	 * @return a new, empty SyncedList
	 */
	public static SyncedList<ItemStack> forItemStack(List<ItemStack> delegate) {
		return new WithSerializer<>(delegate, Serializers.forItemStack());
	}

	/**
	 * <p>Create a new {@code SyncedList} for holding FluidStacks, based on an empty {@link java.util.ArrayList}.</p>
	 * @return a new, empty SyncedList
	 */
	public static SyncedList<FluidStack> forFluidStack() {
		return forFluidStack(new ArrayList<FluidStack>());
	}

	/**
	 * <p>Create a new {@code SyncedList} for holding FluidStacks, based the given List.</p>
	 * <p>It is recommended that the passed in list is empty.</p>
	 * @param delegate the underlying list to use
	 * @return a new, empty SyncedList
	 */
	public static SyncedList<FluidStack> forFluidStack(List<FluidStack> delegate) {
		return new WithSerializer<>(delegate, Serializers.forFluidStack());
	}

	/**
	 * <p>Create a new {@code SyncedList} for holding Strings, based on an empty {@link java.util.ArrayList}.</p>
	 * @return a new, empty SyncedList
	 */
	public static SyncedList<String> forString() {
		return forString(new ArrayList<String>());
	}

	/**
	 * <p>Create a new {@code SyncedList} for holding Strings, based the given List.</p>
	 * <p>It is recommended that the passed in list is empty.</p>
	 * @param delegate the underlying list to use
	 * @return a new, empty SyncedList
	 */
	public static SyncedList<String> forString(List<String> delegate) {
		return new WithSerializer<>(delegate, Serializers.forString());
	}

	/**
	 * <p>Create a new {@code SyncedList} for holding UUIDs, based on an empty {@link java.util.ArrayList}.</p>
	 * @return a new, empty SyncedList
	 */
	public static SyncedList<UUID> forUUID() {
		return forUUID(new ArrayList<UUID>());
	}

	/**
	 * <p>Create a new {@code SyncedList} for holding UUIDs, based the given List.</p>
	 * <p>It is recommended that the passed in list is empty.</p>
	 * @param delegate the underlying list to use
	 * @return a new, empty SyncedList
	 */
	public static SyncedList<UUID> forUUID(List<UUID> delegate) {
		return new WithSerializer<>(delegate, Serializers.forUUID());
	}

	/**
	 * <p>Create a new {@code SyncedList} for holding instances of the given class, based on an empty {@link java.util.ArrayList}.</p>
	 * @param clazz the class of the elements in this list
	 * @return a new, empty SyncedList
	 */
	public static <E extends ByteStreamSerializable> SyncedList<E> create(Class<E> clazz) {
		return withSerializer(Serializers.wrap(clazz));
	}

	/**
	 * <p>Create a new {@code SyncedList} for holding instances of the given class, based the given List.</p>
	 * <p>It is recommended that the passed in list is empty.</p>
	 * @param delegate the underlying list to use
	 * @param clazz the class of the elements in this list
	 * @return a new, empty SyncedList
	 */
	public static <E extends ByteStreamSerializable> SyncedList<E> create(List<E> delegate, Class<E> clazz) {
		return withSerializer(delegate, Serializers.wrap(clazz));
	}

	/**
	 * <p>Create a new {@code SyncedList} for holding instances of class E, based on an empty {@link java.util.ArrayList}.</p>
	 * @param serializer a ByteStreamSerializer for serializing the elements of this list
	 * @return a new, empty SyncedList
	 */
	public static <E> SyncedList<E> withSerializer(ByteStreamSerializer<E> serializer) {
		return withSerializer(new ArrayList<E>(), serializer);
	}

	/**
	 * <p>Create a new {@code SyncedList} for holding instances of class E, based the given List.</p>
	 * <p>It is recommended that the passed in list is empty.</p>
	 * @param delegate the underlying list to use
	 * @param serializer a ByteStreamSerializer for serializing the elements of this list
	 * @return a new, empty SyncedList
	 */
	public static <E> SyncedList<E> withSerializer(List<E> delegate, ByteStreamSerializer<E> serializer) {
		return new WithSerializer<>(delegate, serializer);
	}

	/**
	 * <p>Mark the entire List dirty, meaning it will be re-synced on next query.</p>
	 */
	public void markDirty() {
		sendAll = true;
	}

	/**
	 * <p>Mark the specified index dirty, meaning it will re-synced on next query.</p>
	 * @param index the index
	 */
	public void markDirty(int index) {
		checkElementIndex(index, delegate.size());
		markIndexDirty0(index);
	}

	// implementation

	private static final byte ALL = 0;
	private static final byte CLEAR = 1;
	private static final byte DELTA = 2;

	final List<E> delegate;
	private final BitSet dirtyIndices;
	boolean sendAll = false;

	SyncedList(List<E> delegate) {
		this.delegate = delegate;
		dirtyIndices = new BitSet(delegate.size());
	}

	@Override
	public boolean needsSyncing() {
		return sendAll || !dirtyIndices.isEmpty();
	}

	@Override
	public void writeSyncDataAndReset(MCDataOutputStream out) {
		if (sendAll) {
			int len = delegate.size();

			if (len == 0) {
				out.writeByte(CLEAR);
			} else {
				out.writeByte(ALL);
				out.writeVarInt(len);

				for (E element : delegate) {
					writeElement(element, out);
				}
			}
			sendAll = false;
		} else {
			BitSet indices = dirtyIndices;
			int idx = 0;

			while ((idx = indices.nextSetBit(idx + 1)) >= 0) {
				out.writeVarInt(idx);
				writeElement(delegate.get(idx), out);
			}
			out.writeVarInt(-1);
		}
		dirtyIndices.clear();
	}

	@Override
	public void readSyncData(MCDataInputStream in) {
		int mode = in.readByte();
		switch (mode) {
			case CLEAR:
				delegate.clear();
				break;
			case ALL:
				delegate.clear();
				int len = in.readVarInt();
				for (int i = 0; i < len; i++) {
					delegate.add(readElement(in));
				}
				break;
			case DELTA:
				int idx;
				do {
					idx = in.readVarInt();
					if (idx == -1) {
						break;
					}
					delegate.set(idx, readElement(in));
				} while (true);

		}
	}

	abstract void writeElement(E element, MCDataOutputStream out);
	abstract E readElement(MCDataInputStream in);

	final void markIndexDirty0(int idx) {
		if (!sendAll) {
			dirtyIndices.set(idx);
		}
	}

	@Override
	public void add(int index, @SuppressWarnings("NullableProblems") E element) { // not sure what the warning is about
		delegate.add(index, element);
		sendAll = true;
	}

	@Override
	public boolean add(E e) {
		add(delegate.size(), e);
		return true;
	}

	@Override
	public E set(int index, @SuppressWarnings("NullableProblems") E element) {
		E res = delegate.set(index, element);
		markIndexDirty0(index);
		return res;
	}

	@Override
	public E remove(int index) {
		E res = delegate.remove(index);
		sendAll = true;
		return res;
	}

	@Override
	public boolean remove(Object o) {
		if (delegate.remove(o)) {
			sendAll = true;
			return true;
		}
		return false;
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

	@Override
	public void clear() {
		if (delegate.size() != 0) {
			sendAll = true;
		}
		delegate.clear();
	}

	@Override
	public boolean addAll(@NotNull Collection<? extends E> c) {
		return addAll(delegate.size(), c);
	}

	@Override
	public boolean addAll(int index, @NotNull Collection<? extends E> c) {
		if (delegate.addAll(index, c)) {
			sendAll = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean removeAll(@NotNull Collection<?> c) {
		if (delegate.removeAll(c)) {
			sendAll = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean retainAll(@NotNull Collection<?> c) {
		if (delegate.retainAll(c)) {
			sendAll = true;
			return true;
		}
		return false;
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public E get(int index) {
		return delegate.get(index);
	}

	@Override
	public int indexOf(Object o) {
		return delegate.indexOf(o);
	}

	@SuppressWarnings("SuspiciousMethodCalls")
	@Override
	public int lastIndexOf(Object o) {
		return delegate.indexOf(o);
	}

	@NotNull
	@Override
	public Iterator<E> iterator() {
		return listIterator(0);
	}

	@NotNull
	@Override
	public ListIterator<E> listIterator() {
		return listIterator(0);
	}

	@NotNull
	@Override
	public ListIterator<E> listIterator(int index) {
		return new Itr(delegate.listIterator(index));
	}

	private final class Itr implements ListIterator<E> {

		private final ListIterator<E> delegate;
		private boolean forward;

		Itr(ListIterator<E> delegate) {
			this.delegate = delegate;
		}

		@Override
		public boolean hasNext() {
			return delegate.hasNext();
		}

		@Override
		public E next() {
			forward = true;
			return delegate.next();
		}

		@Override
		public int nextIndex() {
			return delegate.nextIndex();
		}

		@Override
		public boolean hasPrevious() {
			return delegate.hasPrevious();
		}

		@Override
		public E previous() {
			forward = false;
			return delegate.previous();
		}

		@Override
		public int previousIndex() {
			return delegate.previousIndex();
		}

		@Override
		public void remove() {
			delegate.remove();
			sendAll = true;
		}

		@Override
		public void set(E e) {
			int idx = !forward ? delegate.previousIndex() + 1 : delegate.nextIndex() - 1;
			delegate.set(e);
			markIndexDirty0(idx);
		}

		@Override
		public void add(@SuppressWarnings("NullableProblems") E e) {
			delegate.add(e);
			sendAll = true;
		}
	}

	@NotNull
	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return new SubList<>(this, fromIndex, toIndex);
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	@Override
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	private static class SubList<E> extends SyncedList<E> {

		private final SyncedList<E> parent;
		private final int fromIndex;
		private final int toIndex;

		public SubList(SyncedList<E> parent, int fromIndex, int toIndex) {
			super(parent.delegate.subList(fromIndex, toIndex));
			this.parent = parent;
			this.fromIndex = fromIndex;
			this.toIndex = toIndex;
		}

		@NotNull
		@Override
		public List<E> subList(int fromIndex, int toIndex) {
			return parent.subList(this.fromIndex + fromIndex, this.fromIndex + toIndex);
		}

		@Override
		void writeElement(E element, MCDataOutputStream out) {
			parent.writeElement(element, out);
		}

		@Override
		E readElement(MCDataInputStream in) {
			return parent.readElement(in);
		}
	}

	private static class WithSerializer<E> extends SyncedList<E> {

		private final ByteStreamSerializer<E> serializer;

		public WithSerializer(List<E> delegate, ByteStreamSerializer<E> serializer) {
			super(delegate);
			this.serializer = serializer;
		}

		@Override
		void writeElement(E element, MCDataOutputStream out) {
			serializer.write(element, out);
		}

		@Override
		E readElement(MCDataInputStream in) {
			return serializer.read(in);
		}
	}

}
