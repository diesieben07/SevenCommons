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
public abstract class SyncedList<E> implements List<E>, Syncable {

	public static SyncedList<ItemStack> forItemStack() {
		return forItemStack(new ArrayList<ItemStack>());
	}

	public static SyncedList<ItemStack> forItemStack(List<ItemStack> delegate) {
		return new WithSerializer<>(delegate, Serializers.forItemStack());
	}

	public static SyncedList<FluidStack> forFluidStack() {
		return forFluidStack(new ArrayList<FluidStack>());
	}

	public static SyncedList<FluidStack> forFluidStack(List<FluidStack> delegate) {
		return new WithSerializer<>(delegate, Serializers.forFluidStack());
	}

	public static SyncedList<String> forString() {
		return forString(new ArrayList<String>());
	}

	public static SyncedList<String> forString(List<String> delegate) {
		return new WithSerializer<>(delegate, Serializers.forString());
	}

	public static SyncedList<UUID> forUUID() {
		return forUUID(new ArrayList<UUID>());
	}

	public static SyncedList<UUID> forUUID(List<UUID> delegate) {
		return new WithSerializer<>(delegate, Serializers.forUUID());
	}

	public static <E extends ByteStreamSerializable> SyncedList<E> create(Class<E> clazz) {
		return withSerializer(Serializers.wrap(clazz));
	}

	public static <E extends ByteStreamSerializable> SyncedList<E> create(List<E> delegate, Class<E> clazz) {
		return withSerializer(delegate, Serializers.wrap(clazz));
	}

	public static <E> SyncedList<E> withSerializer(ByteStreamSerializer<E> serializer) {
		return withSerializer(new ArrayList<E>(), serializer);
	}

	public static <E> SyncedList<E> withSerializer(List<E> delegate, ByteStreamSerializer<E> serializer) {
		return new WithSerializer<>(delegate, serializer);
	}

	private static final byte ALL = 0;
	private static final byte CLEAR = 1;
	private static final byte DELTA = 2;

	final List<E> delegate;
	private final BitSet dirtyIndices;
	boolean sizeChanged = false;

	SyncedList(List<E> delegate) {
		this.delegate = delegate;
		dirtyIndices = new BitSet(delegate.size());
	}

	@Override
	public boolean needsSyncing() {
		return sizeChanged || dirtyIndices.isEmpty();
	}

	@Override
	public void writeSyncDataAndReset(MCDataOutputStream out) {
		if (sizeChanged) {
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
			sizeChanged = false;
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

	final void markIndexDirty(int idx) {
		if (!sizeChanged) {
			dirtyIndices.set(idx);
		}
	}

	@Override
	public void add(int index, E element) {
		delegate.add(index, element);
		sizeChanged = true;
	}

	@Override
	public boolean add(E e) {
		add(delegate.size(), e);
		return true;
	}

	@Override
	public E set(int index, E element) {
		E res = delegate.set(index, element);
		markIndexDirty(index);
		return res;
	}

	@Override
	public E remove(int index) {
		E res = delegate.remove(index);
		sizeChanged = true;
		return res;
	}

	@Override
	public boolean remove(Object o) {
		if (delegate.remove(o)) {
			sizeChanged = true;
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
			sizeChanged = true;
		}
		delegate.clear();
	}

	@Override
	public boolean addAll(@NotNull Collection<? extends E> c) {
		return addAll(delegate.size(), c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		if (delegate.addAll(index, c)) {
			sizeChanged = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		if (delegate.removeAll(c)) {
			sizeChanged = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		if (delegate.retainAll(c)) {
			sizeChanged = true;
			return true;
		}
		return false;
	}

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
			sizeChanged = true;
		}

		@Override
		public void set(E e) {
			int idx = !forward ? delegate.previousIndex() + 1 : delegate.nextIndex() - 1;
			delegate.set(e);
			markIndexDirty(idx);
		}

		@Override
		public void add(E e) {
			delegate.add(e);
			sizeChanged = true;
		}
	}

	@NotNull
	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return new SubList<E>(this, fromIndex, toIndex);
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
