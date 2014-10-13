package de.take_weiland.mods.commons.internal.sync.impl;

import com.google.common.base.Objects;
import com.google.common.reflect.TypeToken;
import de.take_weiland.mods.commons.net.MCDataInputStream;
import de.take_weiland.mods.commons.net.MCDataOutputStream;
import de.take_weiland.mods.commons.sync.ContainerSyncer;
import de.take_weiland.mods.commons.sync.HandleSubclasses;
import de.take_weiland.mods.commons.sync.PropertySyncer;

import java.lang.reflect.Type;
import java.util.EnumSet;

/**
 * @author diesieben07
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class EnumSetSyncer implements PropertySyncer<EnumSet>, HandleSubclasses {

	private static final Type iterableType = Iterable.class.getTypeParameters()[0];

	static Class getEnumSetType(Type enumSetType) {
		Class clazz = TypeToken.of(enumSetType).resolveType(iterableType).getRawType();
		if (!clazz.isEnum()) {
			throw new RuntimeException("Could not resolve EnumSet type, got " + clazz);
		}
		return clazz;
	}

	private final Class clazz;
	private EnumSet companion;

	public EnumSetSyncer(Type enumSetType) {
		clazz = getEnumSetType(enumSetType);
	}

	@Override
	public boolean hasChanged(EnumSet value) {
		return !Objects.equal(value, companion);
	}

	@Override
	public void writeAndUpdate(EnumSet value, MCDataOutputStream out) {
		out.writeEnumSet(value);
		if (value == null) {
			companion = null;
		} else if (companion == null) {
			companion = value.clone();
		} else {
			companion.clear();
			companion.addAll(value);
		}
	}

	@Override
	public EnumSet read(MCDataInputStream in) {
		return in.readEnumSet(clazz);
	}

	public static final class Contents implements ContainerSyncer<EnumSet> {

		private final Class clazz;
		private EnumSet companion;

		public Contents(Type enumSetType) {
			clazz = getEnumSetType(enumSetType);
		}

		@Override
		public boolean hasChanged(EnumSet value) {
			return !Objects.equal(value, companion);
		}

		@Override
		public void writeAndUpdate(EnumSet value, MCDataOutputStream out) {
			out.writeEnumSet(value);
			if (companion == null) {
				companion = value.clone();
			} else {
				companion.clear();
				companion.addAll(value);
			}
		}

		@Override
		public void read(EnumSet value, MCDataInputStream in) {
			in.readEnumSet(clazz, value);
		}
	}
}
