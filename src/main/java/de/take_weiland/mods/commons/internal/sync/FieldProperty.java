package de.take_weiland.mods.commons.internal.sync;

import de.take_weiland.mods.commons.util.JavaUtils;
import sun.misc.Unsafe;

/**
 * @author diesieben07
 */
public final class FieldProperty extends AbstractProperty {

	private static final Unsafe unsafe = JavaUtils.getUnsafe();
	private final long offset;
	private final Object target;

	public FieldProperty(long offset, Object target) {
		this.offset = offset;
		this.target = target;
	}

	@Override
	public Object get() {
		return unsafe.getObject(target, offset);
	}

	@Override
	public void set(Object value) {
		unsafe.putObject(target, offset, value);
	}
}
