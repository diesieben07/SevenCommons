package de.take_weiland.mods.commons.sync.impl;

import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.ValueSyncer;

/**
 * @author diesieben07
 */
public final class EnumSyncer<E extends Enum<E>> implements ValueSyncer<E> {

	private final Class<E> clazz;

	public EnumSyncer(Class<E> clazz) {
		this.clazz = clazz;
	}

	@Override
	public boolean hasChanged(E value, Object data) {
		return value != data;
	}

	@Override
	public Object writeAndUpdate(E value, MCDataOutputStream out, Object data) {
		out.writeEnum(value);
		return value;
	}

	@Override
	public E read(MCDataInputStream in, Object data) {
		return in.readEnum(clazz);
	}

}
