package de.take_weiland.mods.commons.meta;

import de.take_weiland.mods.commons.util.JavaUtils;

/**
 * @author diesieben07
 */
abstract class AbstractArrayProperty<T> extends AbstractProperty<T> {

	private final T[] values;

	AbstractArrayProperty(int shift, T[] values) {
		super(shift, values.length);
		this.values = values;
	}

	@Override
	T value0(int metadata) {
		return JavaUtils.get(values, metadata);
	}
}
