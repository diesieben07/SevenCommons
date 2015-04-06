package de.take_weiland.mods.commons.meta;

import java.lang.reflect.Array;
import java.util.Set;

/**
 * @author diesieben07
 */
class DualValueProperty<T> extends GenericProperty<T> {

	private final int mask;
	private final T a;
	private final T b;

	DualValueProperty(int shift, T a, T b) {
		this.a = a;
		this.b = b;
		this.mask = 1 << shift;
	}

	@Override
	public T value(int metadata) {
		return (metadata & mask) == 0 ? a : b;
	}

	@Override
	public int toMeta(T value, int previousMeta) {
		return a.equals(value) ? previousMeta : mask | previousMeta;
	}

	@Override
	public boolean hasDistinctValues() {
		return true;
	}

	private T[] values;

	@Override
	public Set<T> values() {
		return values == null ? (values = createValues()) : values;
	}

	@SuppressWarnings("unchecked") // unnecessary casts
	private T[] createValues() {
		Class<? extends T> classA = (Class<? extends T>) a.getClass();
		Class<? extends T> classB = (Class<? extends T>) b.getClass();
		Class<? extends T> arrayClass = classA.isAssignableFrom(classB) ? classB : classA;

		T[] arr = (T[]) Array.newInstance(arrayClass, 2);
		arr[0] = a;
		arr[1] = b;
		return arr;
	}
}
