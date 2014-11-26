package de.take_weiland.mods.commons.sync.impl;

import com.google.common.base.Objects;
import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.ContentSyncer;
import de.take_weiland.mods.commons.sync.ValueSyncer;

import java.lang.reflect.Type;
import java.util.EnumSet;

/**
 * @author diesieben07
 */
public final class EnumSetSyncer<E extends Enum<E>> implements ValueSyncer<EnumSet<E>> {

	private static final Type iterableType = Iterable.class.getTypeParameters()[0];

	static Class getEnumSetType(Type enumSetType) {
		Class clazz = TypeToken.of(enumSetType).resolveType(iterableType).getRawType();
		if (!clazz.isEnum()) {
			throw new RuntimeException("Could not resolve EnumSet type, got " + clazz);
		}
		return clazz;
	}

	private final Class<E> clazz;

	public EnumSetSyncer(Class<E> clazz) {
		this.clazz = clazz;
	}

	@Override
	public boolean hasChanged(EnumSet<E> value, Object data) {
		return !Objects.equal(value, data);
	}

	@Override
	public Object writeAndUpdate(EnumSet<E> value, MCDataOutputStream out, Object data) {
		out.writeEnumSet(value);

		@SuppressWarnings("unchecked")
		EnumSet<E> companion = (EnumSet<E>) data;

		if (value == null) {
			companion = null;
		} else if (companion == null) {
			companion = value.clone();
		} else {
			companion.clear();
			companion.addAll(value);
		}
		return companion;
	}

	@Override
	public EnumSet<E> read(MCDataInputStream in, Object data) {
		return in.readEnumSet(clazz);
	}

	public static final class Contents<E extends Enum<E>> implements ContentSyncer<EnumSet<E>> {

		private final Class<E> clazz;

		public Contents(Class<E> clazz) {
			this.clazz = clazz;
		}

		@Override
		public boolean hasChanged(EnumSet<E> value, Object data) {
			return !Objects.equal(value, data);
		}

		@Override
		public Object writeAndUpdate(EnumSet<E> value, MCDataOutputStream out, Object data) {
			out.writeEnumSet(value);

			@SuppressWarnings("unchecked")
			EnumSet<E> companion = (EnumSet<E>) data;

			if (companion == null) {
				companion = value.clone();
			} else {
				companion.clear();
				companion.addAll(value);
			}
			return companion;
		}

		@Override
		public void read(EnumSet<E> value, MCDataInputStream in, Object data) {
			in.readEnumSet(clazz, value);
		}
	}

}
