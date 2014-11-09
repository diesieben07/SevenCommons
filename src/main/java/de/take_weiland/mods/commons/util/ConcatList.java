package de.take_weiland.mods.commons.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNullableByDefault;
import java.util.*;

/**
 * @author diesieben07
 */
@ParametersAreNullableByDefault
final class ConcatList<T> extends AbstractList<T> {

	private final List<? extends T> a;
	private final List<? extends T> b;

	ConcatList(@Nonnull List<? extends T> a, @Nonnull List<? extends T> b) {
		this.a = a;
		this.b = b;
	}

	@Nonnull
	@Override
	public T get(int index) {
		int aSize = a.size();
		return index < aSize ? a.get(index) : b.get(aSize + index);
	}

	@Override
	public int size() {
		return a.size() + b.size();
	}

	@Override
	public void clear() {
		a.clear();
		b.clear();
	}

	@Override
	protected void removeRange(int fromIndex, int toIndex) {
		int aSize = a.size();
		if (toIndex <= aSize) {
			a.subList(fromIndex, toIndex).clear();
		} else if (fromIndex >= aSize) {
			b.subList(fromIndex - aSize, toIndex - aSize).clear();
		} else {
			a.subList(fromIndex, aSize).clear();
			b.subList(0, toIndex - aSize).clear();
		}
	}

	@Nonnull
	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		int aSize = a.size();
		if (toIndex <= aSize) {
			return unmodifiable(a.subList(fromIndex, toIndex));
		} else if (fromIndex >= aSize) {
			return unmodifiable(b.subList(fromIndex - aSize, toIndex - aSize));
		} else {
			int bSize = b.size();
			List<? extends T> first = fromIndex == 0 ? a : a.subList(fromIndex, aSize);
			List<? extends T> second = toIndex == (aSize + bSize) ? b : b.subList(0, toIndex - aSize);
			return new ConcatList<>(first, second);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> List<T> unmodifiable(@Nonnull List<? extends T> list) {
		// cast is safe, because List is immutable
		return list instanceof ImmutableList ? (List<T>) list : Collections.unmodifiableList(list);
	}

	@Nonnull
	@Override
	public Iterator<T> iterator() {
		return Iterators.concat(a.iterator(), b.iterator());
	}

	@Override
	public int indexOf(Object o) {
		int idx = a.indexOf(o);
		if (idx < 0) {
			idx =  b.indexOf(o);
			return idx < 0 ? -1 : idx + a.size();
		} else {
			return idx;
		}
	}

	@Override
	public int lastIndexOf(Object o) {
		int idx = b.lastIndexOf(o);
		if (idx < 0) {
			return a.lastIndexOf(o);
		} else {
			return idx + a.size();
		}
	}

	@Override
	public boolean removeAll(@Nonnull Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(@Nonnull Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(@Nonnull Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, @Nonnull Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}
}
