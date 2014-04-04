package de.take_weiland.mods.commons.meta;

/**
 * @author diesieben07
 */
class DualValueProperty<T> implements MetadataProperty<T> {

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
}
